import React from 'react';
import { t } from '../../i18n';

import classNames from 'classnames';

import Typography from '@mui/material/Typography';





import TextField from '@mui/material/TextField';



import Grid from '@mui/material/Grid';









import SnackbarContent from '@mui/material/SnackbarContent';
import WarningIcon from '@mui/icons-material/Warning';

import Divider from '@mui/material/Divider';



export class VenCreateConfirmationStep extends React.Component {






  render() {
    const {classes, identification, authentication} = this.props;



    var WarningSnackbar = (props) => {
      return (
      <SnackbarContent className={ classes.warning }
                       style={ { maxWidth: 'none' } }
                       message={ <span id="client-snackbar" className={ classes.message }><WarningIcon style={ { width: 40, height: 40, marginRight: 60 } }className={ classNames( classes.icon, classes.iconVariant ) }/> { props.message }</span> } />
      );
    }
    var venId = authentication.authenticationVenId;
    var noGenerateCertificateWarningView = null;
    var generateCertificateWarningView = null;
    var authenticationTypeWarningView = null;
    if ( identification.needCertificateGeneration !== "no" ) {
      authenticationTypeWarningView = <Grid container spacing={ 3 }>
                                        <Grid size={3} />
                                        <Grid size={6}>
                                          <WarningSnackbar message={ <Typography gutterBottom style={ { color: 'white' } }>
                                                                       <strong>{ t( 'auth.warn.loginRequired' ) }<br/> { t( 'venCreate.warn.venIdIsLogin' ) }</strong>
                                                                     </Typography> } />
                                        </Grid>
                                        <Grid size={3} />
                                      </Grid>
      venId = t( 'common.generated' )
       generateCertificateWarningView = <Grid container spacing={ 3 }>
                                         <Grid size={3} />
                                         <Grid size={6}>
                                           <WarningSnackbar message={ <Typography gutterBottom style={ { color: 'white' } }>
                                                                        <strong>{ t( 'venCreate.warn.certGenerated' ) }<br/> { t( 'venCreate.warn.certNoRegen' ) }</strong>
                                                                      </Typography> } />
                                         </Grid>
                                         <Grid size={3} />
                                       </Grid>

    } else {
      authenticationTypeWarningView = <Grid container spacing={ 3 }>
                                        <Grid size={3} />
                                        <Grid size={6}>
                                          <WarningSnackbar message={ <Typography gutterBottom style={ { color: 'white' } }>
                                                                       <strong>{ t( 'auth.warn.x509Required' ) }<br/></strong>
                                                                     </Typography> } />
                                        </Grid>
                                        <Grid size={3} />
                                      </Grid>

      noGenerateCertificateWarningView = <Grid container spacing={ 3 }>
                                           <Grid size={3} />
                                           <Grid size={6}>
                                             <WarningSnackbar message={ <Typography gutterBottom style={ { color: 'white' } }>
                                                                          <strong>{ t( 'venCreate.warn.certNotGenerated' ) }<br/> { t( 'venCreate.warn.venIdMustMatch' ) }</strong>
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
              <TextField label={ t( 'venCreate.profile' ) }
                         value={ identification.venOadrProfile }
                         className={ classes.textField }
                         margin="dense"
                         variant="outlined"
                         fullWidth={ true }
                         slotProps={{
                           input: { readOnly: true }
                         }} />
            </Grid>
            <Grid size={1}>
              <TextField label={ t( 'auth.authentication' ) }
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
              <TextField label={ t( 'auth.commonName' ) }
                         value={ identification.venCommonName }
                         className={ classes.textField }
                         margin="dense"
                         variant="outlined"
                         fullWidth={ true }
                         slotProps={{
                           input: { readOnly: true }
                         }} />
            </Grid>
            <Grid size={3}>
              <TextField label={ t( 'venCreate.venId' ) }
                         value={ venId }
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

export default VenCreateConfirmationStep;
