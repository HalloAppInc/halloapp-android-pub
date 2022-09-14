import AWS from 'aws-sdk'
import unzipper from 'unzipper'

import Util from './Util'

// Browser can't pull from shared credential file
import credentials from './Credentials'
/**
export interface CredentialsOptions {
    accessKeyId: string
    secretAccessKey: string
    sessionToken?: string
}
 */

AWS.config.credentials = credentials

const LOG_BUCKET_NAME = "halloapp-client-logs"

// Modified from https://gist.github.com/hmontazeri/e9493c2110d4640a5d10429ccbafb616
async function listAllObjectsFromS3Bucket(bucket: string) {
  let s3 = new AWS.S3({ apiVersion: '2006-03-01' });
  let isTruncated = true;
  let marker;
  const elements: Set<string> = new Set()
  while(isTruncated) {
    let params: AWS.S3.ListObjectsRequest = { Bucket: bucket };
    if (marker) params.Marker = marker;
    try {
      const response: any = await s3.listObjects(params).promise();
      response.Contents.forEach((item: any) => {
        elements.add(item.Key.split('/')[0]);
      });
      isTruncated = response.IsTruncated;
      if (isTruncated) {
        marker = response.Contents.slice(-1)[0].Key;
      }
  } catch(error) {
      throw error;
    }
  }
  return Array.from(elements);
}

class Wrapper {

    listUsers() {
        return listAllObjectsFromS3Bucket(LOG_BUCKET_NAME)
    }

    getUser(id: string) {
        return new Promise<string[]>((resolve, reject) => {
            let s3 = new AWS.S3({ apiVersion: '2006-03-01' });
            let request: AWS.S3.ListObjectsRequest = {
                Bucket: LOG_BUCKET_NAME,
                Prefix: id,
            }
            s3.listObjects(request, (err, data) => {
                if (err) {
                    console.log("Error listing user's files: " + err);
                    reject(err)
                    return
                }
                if (data.IsTruncated) {
                    console.log("WARNING: file list was truncated")
                }
                let objs = data.Contents ?? []
                let ret = []
                for (let i = 0; i < objs.length; i++) {
                    ret.push(objs[i].Key!)
                }
                resolve(ret)
            })
        })
    }

    getFile(user: string, file: string) {
        return new Promise<string>((resolve, reject) => {
            let s3 = new AWS.S3({ apiVersion: '2006-03-01' });
            let request: AWS.S3.GetObjectRequest = {
                Bucket: LOG_BUCKET_NAME,
                Key: user + "/" + file,
            }
            s3.getObject(request, (err, data) => {
                if (err) {
                    console.log("Error downloading zip: " + err);
                    reject(err)
                    return
                }
                unzipper.Open.buffer(data.Body! as Buffer)
                    .then(res => {
                        let files = res.files.sort((a, b) => a.path > b.path ? 1 : -1) // oldest at top
                        Promise.all(Util.nums(files.length).map(i => {
                            return new Promise<string>((resolve, reject) => {
                                let file = files[i]
                                file.buffer()
                                    .then(res => {
                                        resolve(res.toString())
                                    }).catch(err => {
                                        console.log("Error buffering entry from zip: " + err)
                                        reject(err)
                                    })
                            })
                        })).then(res => {
                            resolve(res.reduce((p, c) => p + '\n' + c))
                        }).catch(err => {
                            reject(err)
                        })
                    }).catch(err => {
                        console.log("Error opening contents as zip: " + err)
                        reject(err)
                    })
            })
        })
    }

    getRawFile(user: string, file: string) {
        return new Promise<Buffer>((resolve, reject) => {
            let s3 = new AWS.S3({ apiVersion: '2006-03-01' });
            let request: AWS.S3.GetObjectRequest = {
                Bucket: LOG_BUCKET_NAME,
                Key: user + "/" + file,
            }
            s3.getObject(request, (err, data) => {
                if (err) {
                    console.log("Error downloading zip: " + err);
                    reject(err)
                } else {
                    resolve(data.Body! as Buffer)
                }
            })
        })
    }
}

export default new Wrapper()