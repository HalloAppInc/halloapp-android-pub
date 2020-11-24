import React, { Component } from 'react';

import { createStyles, Theme, WithStyles, withStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton'

import PersonIcon from '@material-ui/icons/Person'
import ListIcon from '@material-ui/icons/ListAlt'

import { BrowserRouter as Router, Switch, Route, Link, Redirect, useHistory } from "react-router-dom";

import Dropzone from 'react-dropzone'

import UserList from './UserList'
import User from './User'
import Log from './Log';
import Constants from './Constants'
import Search from './Search'

import Logo from './icon.svg'
import LogCache from './LogCache';

const styles = (theme: Theme) => createStyles({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper,
  },
  toolbar: {
    display: 'flex',
    justifyContent: 'space-between',
  },
  actions: {
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
});

interface Props extends WithStyles<typeof styles> {
}

interface State {
  redirect: boolean,
}

class Container extends Component<Props, State>  {

  constructor(props: Props) {
    super(props)

    this.state = {
      redirect: false,
    }
  }

  genInsideRouter() {
    const { classes } = this.props

    if (this.state.redirect) {
      return <Redirect to="/log/local/local" />
    }

    return (
      <div>
        <AppBar position="static" style={{ height: Constants.APP_BAR_HEIGHT, width: "100%", color: "#eee" }}>
          <Toolbar className={classes.toolbar}>
            <Link to="/">
              <img src={Logo} alt="Logo" style={{ width: Constants.APP_BAR_HEIGHT, height: Constants.APP_BAR_HEIGHT }} />
            </Link>
            <Dropzone onDrop={acceptedFiles => this.handleDroppedFiles(acceptedFiles)}>
              {({ getRootProps, getInputProps, isDragActive }) => (
                <section>
                  <div {...getRootProps()}>
                    {isDragActive
                      ? (
                        <div>
                          <input {...getInputProps()} />
                          <p>Drop file to load</p>
                        </div>
                      )
                      : "Log Browser"}
                  </div>
                </section>
              )}
            </Dropzone>
            <div className={classes.actions}>
              <Link to={"/search"}>
                <IconButton>
                  <PersonIcon />
                </IconButton>
              </Link>
              <Link to={"/users"}>
                <IconButton>
                  <ListIcon />
                </IconButton>
              </Link>
            </div>
          </Toolbar>
        </AppBar>
        <Switch>
          <Route path="/users">
            <UserList />
          </Route>
          <Route path="/user/:id">
            <User />
          </Route>
          <Route path="/log/:id/:file">
            <Log />
          </Route>
          <Route path={["/", "/search"]}>
            <Search />
          </Route>
        </Switch>
      </div>
    )
  }

  handleDroppedFiles(acceptedFiles: any) {
    console.log("JACK GOT FILE " + acceptedFiles)
    let file = acceptedFiles[0]
    const reader = new FileReader()
    reader.onabort = () => console.log('file reading was aborted')
    reader.onerror = () => console.log('file reading has failed')
    reader.onload = (event) => {
      const binaryStr = reader.result as ArrayBuffer
      let other = new Uint8Array(binaryStr)
      let chars = []
      for (let i=0; i<other.length; i++) {
        chars.push(String.fromCharCode(other[i]))
      }
      let str = chars.reduce((c, p) => c + p)//String.fromCharCode.apply(null, Array.from(new Uint8Array(binaryStr)))
      console.log("STR: " + str)
      try {
        LogCache.storeLocal(str)
      } catch (e) {
        console.log("Storing logs failed; trying again after clearing")
        localStorage.clear()
        LogCache.storeLocal(str)
      }
      window.location.href = "/log/local/local"
    }
    reader.readAsArrayBuffer(file)
  }

  render() {
    const { classes } = this.props

    return (
      <div className={classes.root}>
        <Router>
          {this.genInsideRouter()}
        </Router>
      </div>
    );
  }
}

export default withStyles(styles)(Container);
