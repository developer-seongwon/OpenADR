

import React from 'react';
import { t } from '../../i18n';

import Typography from '@mui/material/Typography';

import FormControl from '@mui/material/FormControl';
import FormLabel from '@mui/material/FormLabel';

import TextField from '@mui/material/TextField';

import Grid from '@mui/material/Grid';


import MenuItem from '@mui/material/MenuItem';

import Select from '@mui/material/Select';



import Divider from '@mui/material/Divider';


import HelpIcon from '@mui/icons-material/Help';

import UserCreateIdentificationSSLCertificatePanel from '../common/UserCreateIdentificationSSLCertificatePanel'

const labelStyle = {

  boxSizing: 'border-box',
  color: 'rgba(0, 0, 0, 0.54)',
  fontSize: '1rem',
  fontWeight: 400,

  lineHeight: 1,
  transition: 'color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms',
  transform: 'translate(0, 1.5px) scale(0.75)',
  transformOrigin: 'top left',
  top: 0,
  left: 0,
}


export class VenCreateIndentificationStep extends React.Component {


  handleVenCommonNameChange = (e) => {
    var identification = this.props.identification;
    identification.venCommonName = e.target.value;
    this.props.onChange(identification);
  };
  handleVenOadrProfileChange = (e) => {
    var identification = this.props.identification;
    identification.venOadrProfile = e.target.value;
    this.props.onChange(identification);
  };

  handleNeedCertificateGenerationChange = (e) => {
    var identification = this.props.identification;
    identification.needCertificateGeneration = e.target.value;
    this.props.onChange(identification);
  };



  render() {
    const {classes, hasError, identification, vtnConfiguration} = this.props;

    var profileView = []

    for (var key in this.props.oadrProfiles) {
      var value = this.props.oadrProfiles[ key ];
      profileView.push(

        <MenuItem key={ key } value={ key }>
        { value.label }
        </MenuItem>

      )
    }

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
              <FormLabel style={ labelStyle } component="label">
                { t( 'venCreate.venProfile' ) }
              </FormLabel>
              <Select autoWidth={ true }
                      style={ { marginTop: '0' } }
                      value={ identification.venOadrProfile }
                      onChange={ this.handleVenOadrProfileChange }
                      inputProps={ { name: t( 'venCreate.profileInput' ), id: 'oadr_profile_select', } }>
                { profileView }
              </Select>
            </FormControl>
          </Grid>
          <Grid size={4}>
            <FormControl className={ classes.formControl }>
              <TextField required
                         id="oadr_cn_textfield"
                         fullWidth={ true }
                         label={ t( 'venCreate.venCommonName' ) }
                         placeholder="myven.oadr.com"
                         value={ identification.venCommonName }
                         className={ classes.textField }
                         error={ hasError }
                         onChange={ this.handleVenCommonNameChange }
                         slotProps={{
                           inputLabel: { shrink: true, }
                         }} />
            </FormControl>
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
          <Grid size={8}>
            <Divider />
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

export default VenCreateIndentificationStep;
