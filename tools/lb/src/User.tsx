import React from 'react';

import { RouteComponentProps, withRouter, Link } from "react-router-dom";

import { DateTime, Duration } from 'luxon'

import AWS from './AWS'
import Constants from './Constants'

interface ParamTypes {
  id: string,
}

interface Props extends RouteComponentProps<ParamTypes> {
}

interface State {
  logFiles: string[],
}

const MARGIN = 5

function getVersion(s: string) {
  let parts = s.split("Android")
  if (parts.length !== 2) {
    return undefined
  }

  let parts2 = parts[1].split(".")
  if (parts2.length < 2) {
    return undefined
  }

  let ret = ""
  for (let i = 0; i < parts2.length - 1; i++) {
    ret += parts2[i]
    if (i !== parts2.length - 2) {
      ret += "."
    }
  }

  return ret
}

function getTime(s: string) {
  let parts = s.split("/")
  if (parts.length !== 2) {
    return undefined
  }

  let parts2 = parts[1].split("_")
  if (parts.length !== 2) {
    return undefined
  }

  return parts2[0]
}

function isSameDay(a: DateTime, b: DateTime) {
  return a.year === b.year && a.month === b.month && a.day === b.day
}

function getTimeString(isoDateTime?: string): string {
  if (isoDateTime === undefined) {
    return "Failed to parse timestamp"
  }
  let dt = DateTime.fromISO(isoDateTime)
  return dt.toLocaleString(DateTime.TIME_24_SIMPLE)
}

function getDateString(isoDateTime?: string): string {
  if (isoDateTime === undefined) {
    return "Failed to parse timestamp"
  }
  let dt = DateTime.fromISO(isoDateTime)
  let now = DateTime.local()

  if (isSameDay(dt, now)) {
    return "Today"
  }
  if (isSameDay(dt, now.minus(Duration.fromObject({ days: 1 })))) {
    return "Yesterday"
  }
  return dt.toLocaleString(DateTime.DATE_SHORT)
}

class User extends React.Component<Props, State>  {

  constructor(props: Props) {
    super(props)

    this.state = {
      logFiles: [],
    }
  }

  componentDidMount() {
    const id = this.props.match.params.id
    AWS.getUser(id)
      .then(logFiles => {
        this.setState({
          logFiles: logFiles,
        })
      }).catch(err => {
        console.log("Error listing user's logs: " + err)
      })
  }

  render() {

    return (
      <div>
        <div style={{ height: Constants.LOG_HEADER_HEIGHT, display: 'flex', flexDirection: 'column', justifyContent: 'space-around', color: 'white', backgroundColor: Constants.COLOR_SECONDARY.main }}>
          USER: {this.props.match.params.id}
        </div>
        <div>Logs</div>
        {this.state.logFiles.sort((a, b) => a < b ? 1 : -1).map(logFile => {
          let version = getVersion(logFile)
          let rawTime = getTime(logFile)
          return (
            <div key={logFile} style={{ display: 'flex', flexDirection: 'row', justifyContent: 'center' }}>
            <div style={{ margin: MARGIN }}>{getDateString(rawTime)}</div>
            <div style={{ margin: MARGIN }}>{getTimeString(rawTime)}</div>
              <div style={{ margin: MARGIN }}>{version}</div>
              <Link style={{ margin: MARGIN }} to={"/log/" + logFile}>
                {logFile.split("/")[1]}
              </Link>
            </div>
          )
        })}
      </div>
    );
  }
}

export default withRouter(User);
