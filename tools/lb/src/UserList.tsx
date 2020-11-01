import React, { Component } from 'react';

import { createStyles, Theme, WithStyles, withStyles } from '@material-ui/core/styles';

import { Link } from "react-router-dom";

import AWS from './AWS'

const styles = (theme: Theme) => createStyles({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper,
  },
});

interface Props extends WithStyles<typeof styles> {
}

interface State {
  users: string[],
}

class List extends Component<Props, State>  {

  constructor(props: Props) {
    super(props)
    
    this.state = {
      users: [],
    }
  }

  componentDidMount() {
    AWS.listUsers()
    .then(users => {
      this.setState({
        users: users,
      })
    }).catch(err => {
      console.log("Error listing users: " + err)
    })
  }

  render() {
    const { classes } = this.props

    return (
      <div className={classes.root}>
        {this.state.users.map(user => {
          return (
            <div key={user}>
              <Link to={"/user/" + user} >
                {user}
              </Link>
            </div>
          )
        })}
      </div>
    );
  }
}

export default withStyles(styles)(List);
