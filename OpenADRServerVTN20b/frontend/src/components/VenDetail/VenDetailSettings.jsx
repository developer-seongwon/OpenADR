import React from 'react';












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
                <CloudDownloadIcon style={ { marginRight: 15 } }/> RE-REGISTRATION
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
                <CloudDownloadIcon style={ { marginRight: 15 } }/> CANCEL REGISTRATION
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
                <RemoveIcon style={ { marginRight: 15 } }/> CLEAN REGISTRATION
              </Button>
            </Grid>
        </Grid>
        }/>

        <Divider style={ { marginTop: '20px' } } />
        <Grid>
         <Grid container spacing={ 3 }>
           <Grid size={3}>
             <VenTextField className={ classes.textField } field="VenID" value={ ven.username } />
           </Grid>
           <Grid size={3}>
             <VenTextField className={ classes.textField } field="Authentication Method" value={ ven.authenticationType } />
           </Grid>
           <Grid size={3}>
             <VenTextField className={ classes.textField } field="Common Name" value={ ven.commonName } />
           </Grid>
           <Grid size={3}>
             <VenTextField className={ classes.textField } field="Xml Signature" value={ ven.oadrProfil } />
           </Grid>
         </Grid>
       </Grid>

        <Grid>
          <Grid container spacing={ 3 }>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field="Oadr name" value={ ven.oadrName } />
            </Grid>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field="Transport" value={ ven.transport } />
            </Grid>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field="RegistrationID" value={ ven.registrationId } />
            </Grid>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field="Push Url" value={ ven.pushUrl } />
            </Grid>
          </Grid>
        </Grid>
        <Grid>
          <Grid container spacing={ 3 }>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field="Pull Model" value={ ven.httpPullModel } />
            </Grid>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field="Report Only" value={ ven.reportOnly } />
            </Grid>
            <Grid size={3}>
              <VenTextField className={ classes.textField } field="Xml Signature" value={ ven.xmlSignature } />
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
