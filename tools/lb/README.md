

# Log Browser Setup

## [Download nvm](https://github.com/nvm-sh/nvm) if you don't have it

TL;DR: `curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.36.0/install.sh | bash`

## Install node if you don't have it

`nvm install node`

## Checkout the code if you don't have it

`git clone git@github.com:HalloAppInc/halloapp-android.git`

This is part of the android repo in tools/lb

NOTE: We assume the schemas repo is checked out in the same directory as the android repo

## Install dependencies

`npm i[nstall]`

## Specify AWS Credentials

You need access to AWS S3 on an account. Use this account's credentials to access the logs.
These are exported as fields on an object. This is not checked in because it is authentication material.
Your `Credentials.ts` might looks like this:

```typescript
export default {
    accessKeyId: "XXXXXXXXXX",
    secretAccessKey: "XXXXXXXXXXXXXXXXXX",
}
```

The `Credentials.ts` lives right at `src/Credentials.ts` and is in the `.gitignore`

# Run the app (in development mode)

`npm start`

A new browser tab will open with the app running.

If it does not appear quickly, look at the terminal to see if compilation has completed
and try going to http://localhost:3000/

## Working around local storage limits

If you see a message which looks like this:

```
Unhandled Rejection (QuotaExceededError): Failed to execute 'setItem' on 'Storage': Setting the value of 'logcache/<uid>-<phone>/<datetime>_Android<version>.zip' exceeded the quota
```

Then you may have hit the local storage limit. Most modern browsers limit
local storage to around 5MB, and when there are issues in the app we sometimes
create logs which far exceed this limit. You may choose to copy a portion of
the log file into another file and then drag this file to the 'Log Browser'
title area, or you may try modifying the local storage limits. For Chrome I
don't believe there is any way to modify this limit, but in Firefox you can
go to `about:config` and search for `dom.storage.default_quota` (which is measured
in KB) and update this to a higher value so that the limit will not be reached.
