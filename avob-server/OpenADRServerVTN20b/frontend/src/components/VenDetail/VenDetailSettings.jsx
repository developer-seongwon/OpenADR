import React from 'react';
import { t } from '../../i18n';












import TextField from '@mui/material/TextField';

import Grid from '@mui/material/Grid';




import Divider from '@mui/material/Divider';












import Button from '@mui/material/Button';

import RemoveIcon from '@mui/icons-material/Remove';






import CloudDownloadIcon from '@mui/icons-material/CloudDownload';
import AutorenewIcon from '@mui/icons-material/Autorenew';

import Tooltip from '@mui/material/Tooltip';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogActions from '@mui/material/DialogActions';



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
    // regenerateOpen: 인증서 다시 만들기 확인 창
    this.state = { regenerateOpen: false }
   
    


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

  // VTN 이 남겨 둔 인증서 묶음을 다시 받는다(VEN 을 만들 때 받은 것과 같다)
  handleDownloadCredentialsClick = () => {
    this.props.downloadVenCredentials( this.props.ven.username, this.props.ven.commonName );
  }

  handleRegenerateCredentialsOpen = () => {
    this.setState( { regenerateOpen: true } );
  }

  handleRegenerateCredentialsClose = () => {
    this.setState( { regenerateOpen: false } );
  }

  // 새 키로 다시 만든다. 키 방식은 RSA 로 만든다(VEN 만들기의 기본값과 같다)
  handleRegenerateCredentialsConfirm = () => {
    this.setState( { regenerateOpen: false } );
    this.props.regenerateVenCredentials( this.props.ven.username, this.props.ven.commonName, 'rsa' );
  }


  render() {
    const {classes, ven} = this.props;
    // 인증서로 붙는 VEN 만 인증서를 다시 받거나 만들 수 있다. 다시 받기는 VTN 에 남은 게 있을 때만(credentialsAvailable)
    var usesCertificate = ven.authenticationType === 'x509';

    

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
        }
        alwaysActions={ usesCertificate ? (
          <Grid container spacing={ 1.5 }>
            <Grid size={6}>
              {/* 끈 버튼에는 마우스 이벤트가 안 가서 Tooltip 이 안 뜬다. span 으로 감싼다 */}
              <Tooltip title={ ven.credentialsAvailable ? '' : t( 'venDetail.credentialsUnavailable' ) }>
                <span>
                  <Button key="btn_download_credentials"
                          style={ { marginTop: 15 } }
                          variant="outlined"
                          color="primary"
                          fullWidth={true}
                          size="small"
                          disabled={ !ven.credentialsAvailable }
                          onClick={this.handleDownloadCredentialsClick}>
                    <CloudDownloadIcon style={ { marginRight: 15 } }/> { t( 'venDetail.downloadCredentials' ) }
                  </Button>
                </span>
              </Tooltip>
            </Grid>
            <Grid size={6}>
              <Button key="btn_regenerate_credentials"
                      style={ { marginTop: 15 } }
                      variant="outlined"
                      color="secondary"
                      fullWidth={true}
                      size="small"
                      onClick={this.handleRegenerateCredentialsOpen}>
                <AutorenewIcon style={ { marginRight: 15 } }/> { t( 'venDetail.regenerateCredentials' ) }
              </Button>
            </Grid>
          </Grid>
        ) : null }/>

        <Dialog open={ this.state.regenerateOpen } onClose={ this.handleRegenerateCredentialsClose }>
          <DialogTitle>{ t( 'venDetail.regenerate.title' ) }</DialogTitle>
          <DialogContent>
            <DialogContentText sx={ { mb: 1 } }>{ t( 'venDetail.regenerate.body1' ) }</DialogContentText>
            <DialogContentText sx={ { mb: 1 } }>{ t( 'venDetail.regenerate.body2' ) }</DialogContentText>
            <DialogContentText>{ t( 'venDetail.regenerate.body3' ) }</DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={ this.handleRegenerateCredentialsClose }>{ t( 'common.cancel' ) }</Button>
            <Button color="secondary" variant="contained" onClick={ this.handleRegenerateCredentialsConfirm }>
              { t( 'venDetail.regenerate.confirm' ) }
            </Button>
          </DialogActions>
        </Dialog>

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
