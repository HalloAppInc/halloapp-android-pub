import unzipper from 'unzipper'
import Base64 from 'base64-js'

import AWS from './AWS'

class LogCache {
    private getKey(user: string, fileName: string) {
        return "logcache/" + user + "/" + fileName
    }

    getDayOfFile(user: string, fileName: string, day: number): Promise<string> {
        return new Promise((resolve, reject) => {
            this.getFile(user, fileName)
                .then(zipped => {
                    unzipper.Open.buffer(zipped as Buffer)
                        .then(res => {
                            let files = res.files.sort((a, b) => a.path > b.path ? 1 : -1) // oldest at top
                            if (day >= files.length) {
                                reject("Asked for day " + day + " but only got " + files.length + " days")
                            }
                            let file = files[day]
                            file.buffer()
                                .then(res => {
                                    resolve(res.toString())
                                }).catch(err => {
                                    console.log("Error buffering entry from zip: " + err)
                                    reject(err)
                                })
                        }).catch(err => {
                            console.log("Error opening contents as zip: " + err)
                            reject(err)
                        })
                })
                .catch(err => console.log("error fetching file " + err))
        })
    }

    getEntryNames(user: string, fileName: string): Promise<string[]> {
        return new Promise((resolve, reject) => {
            this.getFile(user, fileName)
                .then(zipped => {
                    unzipper.Open.buffer(zipped as Buffer)
                        .then(res => {
                            let files = res.files.sort((a, b) => a.path > b.path ? 1 : -1) // oldest at top
                            resolve(files.map(file => file.path))
                        })
                        .catch(err => console.log("Failed to open zip file " + err))
                })
        })
    }

    getFile(user: string, fileName: string): Promise<Uint8Array> {
        let key = this.getKey(user, fileName)
        let content = localStorage.getItem(key)
        if (content !== undefined && content !== null) {
            console.log("Using cached version for " + user + "/" + fileName)
            return Promise.resolve(Base64.toByteArray(content))
        }

        return new Promise((resolve, reject) => {
            AWS.getRawFile(user, fileName)
                .then(res => {
                    this.saveFile(user, fileName, Base64.fromByteArray(res))
                    resolve(res)
                }).catch(err => {
                    console.log("Error saving file to cache; trying to clear: " + err)
                    localStorage.clear()
                    AWS.getRawFile(user, fileName)
                        .then(res => {
                            console.log("second attempt worked")
                            this.saveFile(user, fileName, Base64.fromByteArray(res))
                            resolve(res)
                        }).catch(err => {
                            console.log("Error saving file err again: " + err)
                            reject(err)
                        })
                })
        })
    }

    saveFile(user: string, fileName: string, contents: string) {
        localStorage.setItem(this.getKey(user, fileName), contents)
    }

    storeLocal(contents: string) {
        localStorage.setItem("local", contents)
    }

    getLocal() {
        return localStorage.getItem("local")
    }
}

export default new LogCache()