import React from 'react';

import { RouteComponentProps, withRouter, Redirect } from "react-router-dom";

import TextField from '@material-ui/core/TextField';
import Autocomplete from '@material-ui/lab/Autocomplete';

import AWS from './AWS'

interface ParamTypes {
  uid?: string,
  phone?: string,
}

interface Props extends RouteComponentProps<ParamTypes> {
}

interface State {
  users: string[],
  redirectId?: string,
}

class Search extends React.Component<Props, State>  {

  constructor(props: Props) {
    super(props)

    this.state = {
      users: [],
    }
  }

  componentDidMount() {
    AWS.listUsers()
    .then(res => {
      this.setState({
        users: res,
      })
    }).catch(err => {
      console.log("err: " + err)
    })
  }

  onInputChange(event: object, value: string, reason: string) {
    if (reason === 'reset') {
      this.setState({
        redirectId: value,
      })
    }
  }

  render() {

    if (this.state.redirectId !== undefined) {
      return (
        <Redirect to={"/user/" + this.state.redirectId} />
      )
    }

    return (
      <div style={{display: 'flex', flexDirection: 'row', justifyContent: 'space-around'}}>
        <Autocomplete
        style={{width: 500}}
        options={this.state.users}
        onInputChange={(event, value, reason) => this.onInputChange(event, value, reason)}
        renderInput={(params: any) => (
          <TextField
            {...params}
            label="User Id or Phone"
            margin="normal"
            variant="outlined"
            InputProps={{ ...params.InputProps, type: 'search' }}
          />
        )}
      />
      </div>
    );
  }
}

export default withRouter(Search);
