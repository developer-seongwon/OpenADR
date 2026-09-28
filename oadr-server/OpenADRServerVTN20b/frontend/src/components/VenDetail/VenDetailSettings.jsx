import React from 'react';
import { t } from '../../i18n';












import TextField from '@mui/material/TextField';

import Grid from '@mui/material/Grid';




import Divider from '@mui/material/Divider';












import Button from '@mui/material/Button';

import RemoveIcon from '@mui/icons-material/Remove';






import CloudDownloadIcon from '@mui/icons-material/CloudDownload';



import VenDetailHeader from './VenDetailHeader'





var VenTextField = (props) => {
  var value = (props.value != null) ? props.value : "";
  return (
    <TextField label={ props.field }
               value={ value }
               className={ props.className }
               margin="normal"
               fullWidth={true}
               slotProps={{
                 input: { readOnly: true, }
               }} />
  );
}




export class VenDetailSettings extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {}
   
    


  }



  

  handleReRegistrationClick = () => {
    this.props.registerPartyRequestReregistration( this.props.ven.username);
  }

  handleCancelRegistrationClick = () => {
    this.props.registerPartyCancelPartyRegistration( this.props.ven.username);
  }

  handleCleanRegistrationClick = () => {
    this.props.cleanRegistration( this.props.ven.username);
  }


  render() {
    const {classes, ven} = this.props;

    

    return (
      <div className={ classes.root } >
        <VenDetailHeader classes={classes} ven={ven} actions={
          <Grid container spacing={ 1.5 }>
            <Grid size={12}>
              <Button key="btn_create"
                      style={ { marginTop: 15 } }
                      variant="outlined"
                      color="primary"
                      fullWidth={true}
                      size="small"
                      onClick={this.handleReRegistrationClick}>
                <CloudDownloadIcon style={ { marginRight: 15 } }/> { t( 'venDetail.reRegistration' ) }
              </Button>
            </Grid>
            <Grid size={6}>
              <Button key="btn_create"
                      style={ { marginTop: 15 } }
                      variant="outlined"
                      color="secondary"
                      fullWidth={true}
                      size="small"
                      onClick={this.handleCancelRegistrationClick}>
                <CloudDownloadIcon style={ { marginRight: 15 } }/> { t( 'venDetail.cancelRegistration' ) }
              </Button>
            </Grid>
            <Grid size={6}>
              <Button key="btn_create"
                      style={ { marginTop: 15 } }
                      variant="outlined"
                      color="secondary"
                      fullWidth={true}
                      size="small"
                      onClick={this.handleCleanRegistrationClick}>
                <RemoveIcon style={ { marginRight: 15 } }/> { t( 'venDetail.cleanRegistration' ) }
              </Button>
            </Grid>
        </Grid>
        }/>

        <Divider style={ { marginTop: '20px' } } />
        <Grid>
         <Grid container spacing={ 3 }>
           <Grid size={3}>
             <VenTextField className={ classes.textField } field={ t( 'venDetail.field.venId' ) } value={ ven.username } />
           </Grid>
           <Grid size={3}>
             <VenTextField className={ classes.textField } field={ t( 'venDetail.field.authMethod' ) } value={ ven.authenticationType } />
           </Grid>
           <Grid size={3}>
             <VenTextField className={ classes.textField } field={ t( 'venDetail.field.commonName' ) } value={ ven.commonName } />
           </Grid>
           <Grid size={3}>
             {/* 값은 oadrProfil 인데 라벨이 Xml Signature 로 붙어 있었다 */}
             <VenTextField className={ classes.textField } field={ t( 'venDetail.field.profile' ) } value={ ven.oadrProfil } />
           </Grid>
         </Grid>
       </Grid>

        <Grid>
          <Grid container spacing={ 3 }>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field={ t( 'venDetail.field.oadrName' ) } value={ ven.oadrName } />
            </Grid>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field={ t( 'venDetail.field.transport' ) } value={ ven.transport } />
            </Grid>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field={ t( 'venDetail.field.registrationId' ) } value={ ven.registrationId } />
            </Grid>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field={ t( 'venDetail.field.pushUrl' ) } value={ ven.pushUrl } />
            </Grid>
          </Grid>
        </Grid>
        <Grid>
          <Grid container spacing={ 3 }>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field={ t( 'venDetail.field.pullModel' ) } value={ ven.httpPullModel } />
            </Grid>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field={ t( 'venDetail.field.reportOnly' ) } value={ ven.reportOnly } />
            </Grid>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field={ t( 'venDetail.field.xmlSignature' ) } value={ ven.xmlSignature } />
            </Grid>
            <Grid size={3}>

            </Grid>
          </Grid>
        </Grid>
      </div>
    );
  }
}

export default VenDetailSettings;
