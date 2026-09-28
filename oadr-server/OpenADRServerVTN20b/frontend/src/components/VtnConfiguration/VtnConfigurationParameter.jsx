import React from 'react';
import { t } from '../../i18n';

import TextField from '@mui/material/TextField';
import FormControl from '@mui/material/FormControl';
import InputAdornment from '@mui/material/InputAdornment';

import Clear from '@mui/icons-material/Clear';
import Done from '@mui/icons-material/Done';

import Divider from '@mui/material/Divider';




function VtnConfigurationTextField( props ) {
  return (
    <TextField label={ props.field }
               defaultValue={ props.value }
               className={ props.className }
               slotProps={{
                 input: { readOnly: true, }
               }} />
  );
}

function VtnConfigurationFeatureField( props ) {
  var str = (props.value) ? t( 'vtnConfig.supported' ) : t( 'vtnConfig.notSupported' )
  var ico = (props.value) ? <Done color="action" /> : <Clear color="action" />
  return (
    <TextField label={ props.field }
               defaultValue={ str }
               className={ props.className }
               slotProps={{
                 input: { readOnly: true, endAdornment: ( <InputAdornment> { ico } </InputAdornment> ), }
               }} />
  );
}

const VtnConfigurationParameter = (props) => {
  const {classes, vtnConfiguration} = props;

  var getTextField = function ( label, field ) {
    var view = null;
    var value = props.vtnConfiguration[ field ];
    if ( props.vtnConfiguration[ field ] != null ) {
      view = (
        <VtnConfigurationTextField className={ classes.textField }
                                   field={ label }
                                   value={ value } />
      )
    }
    return view;
  }

  var getUrlTextField = function () {
    var view = null;
    if ( vtnConfiguration.contextPath != null
      && vtnConfiguration.port != null
      && vtnConfiguration.host != null ) {
      var endpoint20b = 'https://' + vtnConfiguration.host + ':' + vtnConfiguration.port + vtnConfiguration.contextPath + '/OpenADR2/Simple/2.0b';
      var endpoint20a = 'https://' + vtnConfiguration.host + ':' + vtnConfiguration.port + vtnConfiguration.contextPath + '/OpenADR2/Simple';
      view = [
        <FormControl className={ classes.formControl } key="textfield_endpoint20b">
          <VtnConfigurationTextField className={ classes.textField }
                                     field={ t( 'vtnConfig.endpoint20b' ) }
                                     value={ endpoint20b } />
        </FormControl>,
        <FormControl className={ classes.formControl } key="textfield_endpoint20a">
          <VtnConfigurationTextField className={ classes.textField }
                                     field={ t( 'vtnConfig.endpoint20a' ) }
                                     value={ endpoint20a } />
        </FormControl>
      ]
      return view;

    }
  }

  var getFeatureField = function ( label, field ) {
    var view = null;
    var value = props.vtnConfiguration[ field ];
    if ( props.vtnConfiguration[ field ] != null ) {
      view = (
        <VtnConfigurationFeatureField className={ classes.textField }
                                      field={ label }
                                      value={ value } />
      )
    }
    return view;
  }

  return (
  <div className={ classes.root }>
    <FormControl className={ classes.formControl }>
      { getTextField( t( 'vtnConfig.vtnId' ), 'vtnId' ) }
    </FormControl>
    <FormControl className={ classes.formControl }>
      { getTextField( t( 'vtnConfig.pullFrequency' ), 'pullFrequencySeconds' ) }
    </FormControl>
    <Divider style={ { marginBottom: '20px', marginTop: '20px' } } />
    { getUrlTextField() }
    <Divider style={ { marginBottom: '20px', marginTop: '20px' } } />
    <FormControl className={ classes.formControl }>
      { getFeatureField( t( 'vtnConfig.httpsPush' ), 'supportPush' ) }
    </FormControl>
    <FormControl className={ classes.formControl }>
      { getFeatureField( t( 'vtnConfig.unsecuredHttpPush' ), 'supportUnsecuredHttpPush' ) }
    </FormControl>
  </div>
  );
};

export default VtnConfigurationParameter;
