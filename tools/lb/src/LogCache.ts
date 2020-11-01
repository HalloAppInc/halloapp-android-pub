

import AWS from './AWS'

class LogCache {
    private getKey(user: string, fileName: string) {
        return "logcache/" + user + "/" + fileName
    }

    getFile(user: string, fileName: string): Promise<string> {
        let key = this.getKey(user, fileName)
        let content = localStorage.getItem(key)
        if (content !== undefined && content !== null) {
            console.log("Using cached version for " + user + "/" + fileName)
            return Promise.resolve(content)
        }

        return new Promise((resolve, reject) => {
            AWS.getFile(user, fileName)
                .then(res => {
                    this.saveFile(user, fileName, res)
                    resolve(res)
                }).catch(err => {
                    console.log("Error saving file err; trying to clear: " + err)
                    localStorage.clear()
                    AWS.getFile(user, fileName)
                        .then(res => {
                            console.log("second attempt worked")
                            this.saveFile(user, fileName, res)
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
}

export default new LogCache()