

import React from 'react';
import { t } from '../../i18n';

import FormControl from '@mui/material/FormControl';
import FormLabel from '@mui/material/FormLabel';
import FormControlLabel from '@mui/material/FormControlLabel';




import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';



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

export var UserCreateIdentificationSSLCertificatePanel = (props) => {
  const {classes, identification, vtnConfiguration} = props;

  var handleNeedCertificateGenerationChange = (e) => {
    var identification = props.identification;
    identification.needCertificateGeneration = e.target.value;
    props.onChange(identification);
  };

    var generateOptionView = null;
    if ( vtnConfiguration && vtnConfiguration.supportCertificateGeneration ) {
      generateOptionView = [
        <FormControlLabel key="generate_rsa_radio"
                          value="rsa"
                          control={ <Radio color="primary" /> }
                          label={ t( 'cert.generateRsa' ) }
                          labelPlacement="end" />,
        <FormControlLabel key="generate_ecc_radio"
                          value="ecc"
                          control={ <Radio color="primary" /> }
                          label={ t( 'cert.generateEcc' ) }
                          labelPlacement="end" />
      ]
    }

    return (

    <FormControl className={ classes.formControl }>
        <FormLabel style={ labelStyle } component="label">
          { t( 'cert.sslCertificate' ) }
        </FormLabel>
        <RadioGroup aria-label="position"
                    name="position"
                    value={ identification.needCertificateGeneration }
                    onChange={ handleNeedCertificateGenerationChange }
                    row>
          <FormControlLabel value="no"
                            style={ { marginLeft: 0 } }
                            control={ <Radio color="primary" /> }
                            label={ t( 'cert.provideUsername' ) }
                            labelPlacement="end" />
          { generateOptionView }
        </RadioGroup>
      </FormControl>
    );
} 

export default UserCreateIdentificationSSLCertificatePanel;