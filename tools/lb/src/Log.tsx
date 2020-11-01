import React from 'react';
import ReactDOM from 'react-dom'

import Base64 from 'base64-js'

import InputAdornment from '@material-ui/core/InputAdornment';
import TextField from '@material-ui/core/TextField';

import CloseIcon from '@material-ui/icons/Close'

import { RouteComponentProps, withRouter, Link } from "react-router-dom";

import { AutoSizer, Grid } from 'react-virtualized'

import LogCache from './LogCache'
import Constants from './Constants'
import { IconButton } from '@material-ui/core';

import Proto from './proto/compiled'

interface ParamTypes {
  id: string,
  file: string,
}

interface Props extends RouteComponentProps<ParamTypes> {
}

interface LogLine {
  message: string,
  level: string,
}

interface State {
  logs: LogLine[],
  filteredLogs: LogLine[],
  filterText: string,
  findText: string,
  findError: boolean,
  highlightedLine: number,
}

function getColorForLetter(s: string) {
  switch (s) {
    case "E":
      return "#ff0000"
    case "W":
      return "#F57B0B"
    case "I":
      return "#4135C4"
    case "D":
      return "#444444"
    case "V":
      return "#66ff66"
    default:
      return "#aaaaaa"
  }
}

function getLetterFromLine(line: string) {
  let parts = line.split("/")
  if (parts.length < 2) {
    return undefined
  }

  let parts2 = parts[1].split(":")
  if (parts2.length < 2) {
    return undefined
  }

  return parts2[0]
}

function isUpperCase(s: string) {
  return s.toUpperCase() === s
}

function isLowerCase(s: string) {
  return s.toLowerCase() === s
}

function camelCaseToKebabCase(s: string) {
  let ret = ""
  for (let i=0; i<s.length; i++) {
    let c = s.charAt(i)
    if (isUpperCase(c) && i !== 0 && i !== s.length - 1 && (isLowerCase(s.charAt(i+1)) || isLowerCase(s.charAt(i-1)))) {
      ret += "_"
    }
    ret += c.toLowerCase()
  }
  return ret
}

class Log extends React.Component<Props, State>  {

  constructor(props: Props) {
    super(props)

    this.state = {
      logs: [],
      filteredLogs: [],
      filterText: "",
      findText: "",
      findError: false,
      highlightedLine: -1,
    }
    this.onKeyDown = this.onKeyDown.bind(this)
  }

  runFilter(logs: LogLine[], filterText: string) {
    if (filterText === undefined || filterText.length === 0) {
      return logs
    }
    return logs.filter(log => log.message.includes(filterText))
  }

  getVersionFF(fileName: string) {
    let parts = fileName.split("Android")
    if (parts.length !== 2) {
      return undefined
    }

    let parts2 = parts[1].split(".")
    let ret = ""
    for (let i=0; i<parts2.length - 1; i++) {
      ret += parts2[i]
      if (i !== parts2.length - 2) {
        ret += "."
      }
    }
    return ret
  }

  getVersion() {
    return this.getVersionFF(this.props.match.params.file)
  }

  componentDidMount() {
    document.addEventListener("keydown", this.onKeyDown, false);
    const id = this.props.match.params.id
    const file = this.props.match.params.file
    LogCache.getFile(id, file)
      .then(data => {
        let logLines = data.split('\n')
        let logLevels = logLines.map(l => getLetterFromLine(l))
        for (let i = 1; i < logLevels.length; i++) {
          if (logLevels[i] === undefined) {
            logLevels[i] = logLevels[i - 1]
          }
        }
        let logs: LogLine[] = []
        for (let i = 0; i < logLines.length; i++) {
          logs.push({
            message: logLines[i],
            level: logLevels[i] as string,
          })
        }
        let filtered = this.runFilter(logs, this.state.filterText)
        this.setState({
          logs: logs,
          filteredLogs: filtered,
        })
      }).catch(err => {
        console.log("Error fetching file for " + id + "/" + file + ": " + err)
      })
  }

  componentWillUnmount() {
    document.removeEventListener("keydown", this.onKeyDown, false);
  }

  getPath(mid: string) {
    let i = 0
    let c = mid.charAt(i)
    while ((c === "." || c.toUpperCase() !== c) && i < mid.length) {
      i++
      c = mid.charAt(i)
    }
    let endPos = i
    let ss = mid.substr(0, endPos)
    return ss.replace(/\./g, "/")
  }

  getFileName(mid: string) {
    let parts = mid.split("(")
    if (parts.length !== 2) {
      return undefined
    }

    let parts2 = parts[1].split(":")
    if (parts2.length !== 2) {
      return undefined
    }

    return parts2[0]
  }

  getFileLine(mid: string) {
    let parts = mid.split(")")
    if (parts.length !== 2) {
      return undefined
    }

    let parts2 = parts[0].split(":")
    if (parts2.length !== 2) {
      return undefined
    }

    return parts2[1]
  }

  linkify(s: string) {
    let i = s.indexOf("at com.halloapp.")
    if (i >= 0) {
      let startPos = i + 3;
      let pre = s.substr(0, startPos)
      let mid = s.substr(startPos)

      let path = this.getPath(mid)
      let fileName = this.getFileName(mid)
      let fileLine = this.getFileLine(mid)

      if (fileName === "Unknown Source") {
        return s
      }

      let version = "v" + this.getVersion()
      
      return (
        <div>
          {pre}
          <a href={"https://github.com/HalloAppInc/halloapp-android/blob/" + version + "/app/src/main/java/" + path + fileName + "#L" + fileLine}>
            {mid}
          </a>
        </div>
      )
    }
    return this.protofy(s)
  }

  protofy(s: string): any {
    let START_TAG = "<![CLBDATA["
    let END_TAG = "]]>"

    let i = s.indexOf(START_TAG)
    if (i >= 0) {
      let messageTypeChar = s.charAt(i + START_TAG.length)
      let j = s.substr(i + START_TAG.length + 1).indexOf(END_TAG)
      if (j >= 0) {
        let before = s.substr(0, i)
        let afterNoRec = s.substr(i + START_TAG.length + 1 + j + END_TAG.length)
        let after = this.protofy(afterNoRec)
        let messageType: any
        let messageTypeName: string
        if (messageTypeChar === 'P') {
          messageType = Proto.server.Packet
          messageTypeName = "packet"
        } else if (messageTypeChar === 'M') {
          messageType = Proto.server.Msg
          messageTypeName = "msg"
        } else if (messageTypeChar === 'I') {
          messageType = Proto.server.Iq
          messageTypeName = "iq"
        } else if (messageTypeChar === 'A') {
          messageType = Proto.server.Ack
          messageTypeName = "ack"
        } else if (messageTypeChar === 'R') {
          messageTypeName = "auth_result"
          messageType = Proto.server.AuthResult
        } else if (messageTypeChar === '?') {
          return before + "<!Client did not send top-level protobuf type>" + after
        } else {
          return before + "<!We don't recognize this protobuf type>" + after
        }
        try {
          let b64 = s.substr(i + START_TAG.length + 1, j)
          let msg = messageType.decode(Base64.toByteArray(b64))
          let obj = msg.toJSON()
          return (
            <div style={{display: 'flex', flexDirection: 'row', justifyContent: 'start'}}>
              <div>{before}</div>
              <div style={{backgroundColor: "#ccc"}}>{this.messageObjectToString(obj, messageTypeName)}</div>
              <div>{after}</div>
            </div>
          )
        } catch (err) {
          console.log("Error protofying: " + err)
          return before + "<!Failed to parse protobuf>" + after
        }
      }
    }

    return s
  }

  messageObjectToString(obj: any, name: string) {
    let keys = Object.keys(obj)
    let attributes = new Map<string, string>()
    let children: any[][] = []
    for (let i=0; i<keys.length; i++) {
      let key = keys[i]
      let val = obj[key]
      if (val instanceof Object) {
        children.push([camelCaseToKebabCase(key), val])
      } else {
        attributes.set(camelCaseToKebabCase(key), val)
      }
    }

    let ret = "<" + name

    if (attributes.size > 0) {
      let keySet = attributes.keys()
      let next = keySet.next()
      while (!next.done) {
        let key = next.value
        ret += " " + key + "='" + attributes.get(key) + "'"
        next = keySet.next()
      }
    }

    if (children.length <= 0) {
      ret += "/>"
    } else {
      let childStrings = children.map(c => this.messageObjectToString(c[1], c[0]))
      ret += ">" + childStrings.reduce((p, c) => p + c) + "</" + name + ">"
    }

    return ret
  }

  renderRow(index: number, key: string, style: React.CSSProperties) {
    let color = getColorForLetter(this.state.filteredLogs[index].level)
    let line = this.state.filteredLogs[index].message
    let linkified = this.linkify(line)
    return (
      <div key={key} style={{ ...style, display: "flex", justifyContent: "start", fontFamily: "monospace", backgroundColor: index === this.state.highlightedLine ? "#ECFF38" : index % 2 === 0 ? "#fff" : "#eee", paddingLeft: 5 }}>
        <pre style={{ position: "relative", top: /*hack*/ -10, color: color }}>{linkified}</pre>
      </div>
    )
  }

  onFilterChange(event: any) {
    let value = event.target.value
    let filtered = this.runFilter(this.state.logs, value)
    this.setState({
      filterText: value,
      filteredLogs: filtered,
    })
  }

  onFindChange(event: any) {
    let value = event.target.value
    this.findNextOccurrence(value, this.state.highlightedLine, false)
  }

  findNextOccurrence(text: string, startLine: number, backwards: boolean) {
    if (text === "" || text === undefined) {
      this.setState({
        findText: text,
        findError: false,
        highlightedLine: -1,
      })
      return
    }
    if (startLine <= 0) {
      startLine = 0
    }
    let nextOccurrence = -1
    let len = this.state.filteredLogs.length
    for (let i = 0; i < len; i++) {
      let index = (len + startLine + i * (backwards ? -1 : 1)) % len
      if (this.state.filteredLogs[index].message.includes(text)) {
        nextOccurrence = index;
        break;
      }
    }
    this.setState({
      findText: text,
      findError: nextOccurrence === -1,
      highlightedLine: nextOccurrence,
    })
  }

  clearFind() {
    this.setState({
      findText: "",
      findError: false,
      highlightedLine: -1,
    })
  }

  clearFilter() {
    let allLogs = this.state.logs
    this.setState({
      filterText: "",
      filteredLogs: allLogs,
    })
  }

  onKeyDown(event: any) {
    if (event.key === "Enter") {
      this.findNextOccurrence(this.state.findText, this.state.highlightedLine + (event.shiftKey ? -1 : 1), event.shiftKey)
    } else if (event.key === "f" && event.ctrlKey) {
      this.findText?.focus()
      event.preventDefault()
    } else if (event.key === "Escape") {
      if (document.activeElement === ReactDOM.findDOMNode(this.findText)) {
        this.clearFind()
      } else if (document.activeElement === ReactDOM.findDOMNode(this.filterText)) {
        this.clearFilter()
      }
    }
  }

  private filterText?: HTMLElement
  private findText?: HTMLElement

  render() {

    return (
      <div style={{ display: 'flex', flexDirection: 'column', }}>
        <div style={{ height: Constants.LOG_HEADER_HEIGHT, display: 'flex', flexDirection: 'column', justifyContent: 'space-around', color: '#eee', backgroundColor: Constants.COLOR_SECONDARY.main }}>
          <div>USER: <Link to={"/user/" + this.props.match.params.id}>{this.props.match.params.id}</Link></div>
        </div>
        <div style={{ height: Constants.LOG_HEADER_HEIGHT, display: 'flex', flexDirection: 'column', justifyContent: 'space-around', color: '#eee', backgroundColor: Constants.COLOR_SECONDARY.light }}>
          <div style={{ display: 'flex', flexDirection: 'row', justifyContent: 'space-between' }}>
            <TextField
              inputRef={input => this.filterText = input ?? undefined}
              label="Filter"
              margin="normal"
              variant="filled"
              color="secondary"
              value={this.state.filterText}
              onChange={ev => this.onFilterChange(ev)}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => this.clearFilter()}>
                      <CloseIcon />
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
            <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
              FILE: {this.props.match.params.file}
            </div>
            <TextField
              inputRef={input => this.findText = input ?? undefined}
              label="Find"
              margin="normal"
              variant="filled"
              color="secondary"
              error={this.state.findError}
              value={this.state.findText}
              onChange={ev => this.onFindChange(ev)}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => this.clearFind()}>
                      <CloseIcon />
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
          </div>
        </div>
        <AutoSizer style={{ width: window.innerWidth, height: window.innerHeight - Constants.APP_BAR_HEIGHT - Constants.LOG_HEADER_HEIGHT * 2 - 140 }}>
          {({ height, width }) => (
            <Grid
              scrollToRow={this.state.highlightedLine}
              height={height}
              rowHeight={20}
              cellRenderer={({ rowIndex, key, style }) => this.renderRow(rowIndex, key, style)}
              width={width}
              rowCount={this.state.filteredLogs.length}
              columnCount={1}
              columnWidth={window.innerWidth * 10}
            />
          )}
        </AutoSizer>
      </div>
    );
  }
}

export default withRouter(Log);
