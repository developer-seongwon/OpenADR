import React from 'react';

import Typography from '@mui/material/Typography';

import Divider from '@mui/material/Divider';

import Button from '@mui/material/Button';
import AddIcon from '@mui/icons-material/Add';





import { history } from '../../store/configureStore';
import Grid from '@mui/material/Grid';


import FilterPanel from '../common/FilterPanel' 

import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';

import Toolbar from '@mui/material/Toolbar';

var AccountUserTable = (props) => {
  const {classes, app} = props
  console.log(props.app);
  return (
    <Paper className={classes.root}>
      <Toolbar
      className={classes.root}
    >
      <div className={classes.title}>
         <Typography variant="h6" id="tableTitle">
             Users
          </Typography>
      </div>
      <div className={classes.spacer} />
      <div className={classes.actions}>
      </div>
    </Toolbar>
      <Table className={classes.table}>
        <TableHead>
          <TableRow>
            <TableCell align="right">Common Name</TableCell>
            <TableCell align="right">Username</TableCell>
            <TableCell align="right">Authentication Method</TableCell>
            <TableCell align="right">Roles</TableCell>
            <TableCell align="right"></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {app.map(row => (
            <TableRow key={row.id}>
              <TableCell align="right">{row.commonName}</TableCell>
              <TableCell align="right">{row.username}</TableCell>
              <TableCell align="right">{row.authenticationType}</TableCell>
              <TableCell align="right">{row.roles.map(role => {
                return (<span key={role}>
                    {role} <br/>
                    </span>
                  );
              })}</TableCell>
              <TableCell scope="row" align="right">
                  <Button size="small" color="secondary" onClick={props.handleDeleteApp(row.username)}> REMOVE </Button>
              </TableCell>
        
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Paper>
  )

}



export class AccountApp extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {}
  }

  handleCreateAppClick = () => {
    history.push( '/account/app/create' )
  }

  handleDeleteApp = (username) => () => {
    this.props.deleteApp(username)
  }

  render() {
    const {classes, app} = this.props;


    return (
      <div className={ classes.root }>
        <Grid container spacing={ 1 }>
            <Grid container
                  >

              <Grid size={11}>
                <FilterPanel classes={classes}  type="VEN"/>
              </Grid>
              <Grid size={1}>
               <Button key="btn_create"
                        variant="outlined"
                        color="primary"
                        size="small"
                        className={ classes.button }
                        fullWidth={ true } 
                        onClick={ this.handleCreateAppClick }>
                  <AddIcon />New
                </Button>
              </Grid>
             
            </Grid>
          </Grid>
        <Divider style={ { marginBottom: '30px', marginTop: '20px' } } />
        <AccountUserTable app={app} classes={classes} handleDeleteApp={this.handleDeleteApp}/>

      </div>
    );
  }
}

export default AccountApp;
