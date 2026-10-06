

import React from 'react';
import { t } from '../../i18n';

import Typography from '@mui/material/Typography';

import FormControl from '@mui/material/FormControl';

import TextField from '@mui/material/TextField';



import Grid from '@mui/material/Grid';




import Divider from '@mui/material/Divider';


import HelpIcon from '@mui/icons-material/Help';

import UserCreateIdentificationSSLCertificatePanel from '../common/UserCreateIdentificationSSLCertificatePanel'

export class AccountUserCreateIdentificationStep extends React.Component {

  handleNeedCertificateGenerationChange = (e) => {
    var identification = this.props.identification;
    identification.needCertificateGeneration = e.target.value;
    this.props.onChange(identification);
  };

  handleCommonNameChange = (e) => {
    var identification = this.props.identification;
    identification.commonName = e.target.value;
    this.props.onChange(identification);
  }


  render() {
    const {classes, identification, vtnConfiguration} = this.props;
    return (
      <Grid container
            spacing={ 1 }
            sx={{
              justifyContent: "center"
            }}>
        <Grid container spacing={ 3 }>
          <Grid size={2} />
          <Grid size={4}>
             <FormControl className={ classes.formControl }>
              <TextField required
                         id="oadr_cn_textfield"
                         fullWidth={ true }
                         label={ t( 'accountCreate.userCommonName' ) }
                         placeholder="user1.oadr.com"
                         value={ identification.commonName }
                         className={ classes.textField }
                         error={ this.props.hasError }
                         onChange={ this.handleCommonNameChange }
                         slotProps={{
                           inputLabel: { shrink: true, }
                         }} />
            </FormControl>
          </Grid>
          <Grid size={4}>
           
          </Grid>
          <Grid size={2} />
        </Grid>
        <Grid container
              style={ { marginTop: 20 } }
              spacing={ 3 }>
          <Grid size={2} />
          <Grid size={8}>
            <Divider />
          </Grid>
          <Grid size={2} />
        </Grid>
        <Grid container
              style={ { marginTop: 20 } }
              spacing={ 3 }>
          <Grid size={2} />
          <Grid size={8}>
            <UserCreateIdentificationSSLCertificatePanel classes={classes}
              identification={identification}
              vtnConfiguration={vtnConfiguration}
              onChange={this.props.onChange} />
          </Grid>
          <Grid size={2} />
        </Grid>
        <Grid container
              style={ { marginTop: 20 } }
              spacing={ 3 }>
          <Grid size={2} />
          <Grid size={1}>
            <HelpIcon color="disabled" style={ { width: 40, height: 40 } } />
          </Grid>
          <Grid size={6}>
            <Typography variant="caption" gutterBottom>
              { t( 'accountCreate.help.certificate' ) }
            </Typography>
            <Typography variant="caption" gutterBottom>
              { t( 'accountCreate.help.notGenerated' ) }
            </Typography>
            <Typography variant="caption" gutterBottom>
              { t( 'accountCreate.help.generated' ) }
            </Typography>
          </Grid>
          <Grid size={2} />
        </Grid>
      </Grid>
    );
  }
}

export default AccountUserCreateIdentificationStep;
