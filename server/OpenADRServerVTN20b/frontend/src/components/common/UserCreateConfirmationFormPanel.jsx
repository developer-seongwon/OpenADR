import React from 'react';

import classNames from 'classnames';

import Typography from '@mui/material/Typography';





import TextField from '@mui/material/TextField';



import Grid from '@mui/material/Grid';









import SnackbarContent from '@mui/material/SnackbarContent';
import WarningIcon from '@mui/icons-material/Warning';

import Divider from '@mui/material/Divider';



export class UserCreateConfirmationFormPanel extends React.Component {






  render() {
    const {classes, identification, authentication, roles} = this.props;



    var WarningSnackbar = (props) => {
      return (
      <SnackbarContent className={ classes.warning }
                       style={ { maxWidth: 'none' } }
                       message={ <span id="client-snackbar" className={ classes.message }><WarningIcon style={ { width: 40, height: 40, marginRight: 60 } }className={ classNames( classes.icon, classes.iconVariant ) }/> { props.message }</span> } />
      );
    }
    var username = authentication.username;
    var noGenerateCertificateWarningView = null;
    var generateCertificateWarningView = null;
    var authenticationTypeWarningView = null;
    if ( identification.needCertificateGeneration !== "no" ) {
      authenticationTypeWarningView = <Grid container spacing={ 3 }>
                                        <Grid size={3} />
                                        <Grid size={6}>
                                          <WarningSnackbar message={ <Typography gutterBottom style={ { color: 'white' } }>
                                                                       <strong>Authentication using login / password is required.<br/> Username is used as user/app login</strong>
                                                                     </Typography> } />
                                        </Grid>
                                        <Grid size={3} />
                                      </Grid>
      username = '<generated>'
       generateCertificateWarningView = <Grid container spacing={ 3 }>
                                         <Grid size={3} />
                                         <Grid size={6}>
                                           <WarningSnackbar message={ <Typography gutterBottom style={ { color: 'white' } }>
                                                                        <strong>Certificate will be generated and can only be downloaded at the end of this form.<br/> Certificate can't be regenerated.</strong>
                                                                      </Typography> } />
                                         </Grid>
                                         <Grid size={3} />
                                       </Grid>

    } else {
      authenticationTypeWarningView = <Grid container spacing={ 3 }>
                                        <Grid size={3} />
                                        <Grid size={6}>
                                          <WarningSnackbar message={ <Typography gutterBottom style={ { color: 'white' } }>
                                                                       <strong>Authentication using x509 client certificate is required.<br/></strong>
                                                                     </Typography> } />
                                        </Grid>
                                        <Grid size={3} />
                                      </Grid>

      noGenerateCertificateWarningView = <Grid container spacing={ 3 }>
                                           <Grid size={3} />
                                           <Grid size={6}>
                                             <WarningSnackbar message={ <Typography gutterBottom style={ { color: 'white' } }>
                                                                          <strong>Certificate won't be generated.<br/> Provided username MUST match with user/app certificate fingerprint</strong>
                                                                        </Typography> } />
                                           </Grid>
                                           <Grid size={3} />
                                         </Grid>
    }

    return (
      <div>
        <Grid container
              spacing={ 1 }
              sx={{
                justifyContent: "center"
              }}>
          <Grid container spacing={ 3 }>
            <Grid size={2} />
            <Grid size={1}>
             
            </Grid>
            <Grid size={1}>
              <TextField label="Authentication"
                         value={ authentication.authenticationType }
                         className={ classes.textField }
                         margin="dense"
                         variant="outlined"
                         fullWidth={ true }
                         slotProps={{
                           input: { readOnly: true }
                         }} />
            </Grid>
            <Grid size={3}>
              <TextField label="Common Name"
                         value={ identification.commonName }
                         className={ classes.textField }
                         margin="dense"
                         variant="outlined"
                         fullWidth={ true }
                         slotProps={{
                           input: { readOnly: true }
                         }} />
            </Grid>
            <Grid size={3}>
              <TextField label="Username"
                         value={ username }
                         className={ classes.textField }
                         margin="dense"
                         variant="outlined"
                         fullWidth={ true }
                         slotProps={{
                           input: { readOnly: true }
                         }} />
            </Grid>
            <Grid size={2} />
          </Grid>
          <Grid container
                style={ { marginTop: 20, marginBottom: 20 } }
                spacing={ 3 }>
            <Grid size={2} />
            <Grid size={8}>
              <Divider />
            </Grid>
            <Grid size={2} />
          </Grid>
          <Grid container
                style={ { marginTop: 20, marginBottom: 20 } }
                spacing={ 3 }>
            <Grid size={2} />
            <Grid size={8}>
              <TextField label="Roles"
                         value={ roles }
                         className={ classes.textField }
                         margin="dense"
                         variant="outlined"
                         fullWidth={ true }
                         slotProps={{
                           input: { readOnly: true }
                         }} />
            </Grid>
            <Grid size={2} />
          </Grid>
          <Grid container
                style={ { marginTop: 20, marginBottom: 20 } }
                spacing={ 3 }>
            <Grid size={2} />
            <Grid size={8}>
              <Divider />
            </Grid>
            <Grid size={2} />
          </Grid>
          { authenticationTypeWarningView }
          <Grid container
                style={ { marginTop: 20, marginBottom: 20 } }
                spacing={ 3 }>
            <Grid size={2} />
            <Grid size={8}>
              <Divider />
            </Grid>
            <Grid size={2} />
          </Grid>
          { noGenerateCertificateWarningView }
          { generateCertificateWarningView }
        </Grid>
      </div>
    );
  }
}

export default UserCreateConfirmationFormPanel;
