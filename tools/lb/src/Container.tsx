import React, { Component } from 'react';

import { createStyles, Theme, WithStyles, withStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import IconButton from '@material-ui/core/IconButton'

import PersonIcon from '@material-ui/icons/Person'
import ListIcon from '@material-ui/icons/ListAlt'

import { BrowserRouter as Router, Switch, Route, Link } from "react-router-dom";

import UserList from './UserList'
import User from './User'
import Log from './Log';
import Constants from './Constants'
import Search from './Search'

import Logo from './icon.svg'

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
}

/**
 * Open TODOs:
 * 1. Linking to user ids (caching of user list)
 * 2. Cache logs compressed, not uncompressed
 * 3. Show list of exceptions/stack traces in the file
 * 4. XML highlighting
 * 5. Compute protobufs once at load time so that can filter on protobuf contents
 */

class Container extends Component<Props, State>  {

  constructor(props: Props) {
    super(props)

    this.state = {
    }
  }

  genInsideRouter() {
    const { classes } = this.props

    return (
      <div>
        <AppBar position="static" style={{ height: Constants.APP_BAR_HEIGHT, width: "100%", color: "#eee" }}>
          <Toolbar className={classes.toolbar}>
            <Link to="/">
              <img src={Logo} alt="Logo" style={{ width: Constants.APP_BAR_HEIGHT, height: Constants.APP_BAR_HEIGHT }}/>
            </Link>
              Log Browser
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
