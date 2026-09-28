import React from 'react';
import { t } from '../../i18n';










import Button from '@mui/material/Button';








import CloudDownloadIcon from '@mui/icons-material/CloudDownload';


import EventDetailHeader from './EventDetailHeader'
import Divider from '@mui/material/Divider';
import TextField from '@mui/material/TextField';

import Grid from '@mui/material/Grid';




import {formatTimestamp} from '../../utils/time'


var EventTextField = (props) => {
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

export class EventDetailDescriptor extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {}


  }

  handlePublishEventClick = () => {
    this.props.publishEvent(this.props.event.id);
  }

  handleActiveEventClick = () => {
    this.props.activeEvent(this.props.event.id);
  }
 
  handleCancelEventClick = () => {
    this.props.cancelEvent(this.props.event.id)
  }

  render() {
    const {classes, event} = this.props;
    if(Object.keys(event).length === 0) return null;
    
    var createdDatetime = formatTimestamp(event.createdTimestamp);
    var lastUpdateDatetime = formatTimestamp(event.lastUpdateTimestamp);


    return (
      <div className={ classes.root } >
        <EventDetailHeader classes={classes} event={event} actions={<Grid container spacing={ 3 }>

          {(!event.published) ? <Grid size={4}>
              <Button key="btn_create"
                      style={ { marginTop: 15 } }
                      variant="outlined"
                      color="primary"
                      fullWidth={true}
                      size="small"
                      onClick={this.handlePublishEventClick}>
                <CloudDownloadIcon style={ { marginRight: 15 } }/> { t( 'eventDetail.publish' ) }
              </Button>
            </Grid> : null}

          {(event.published && event.descriptor.state === "ACTIVE") ? <Grid size={4}>
              <Button key="btn_create"
                      style={ { marginTop: 15 } }
                      variant="outlined"
                      color="secondary"
                      fullWidth={true}
                      size="small"
                      onClick={this.handleCancelEventClick}>
                <CloudDownloadIcon style={ { marginRight: 15 } }/> { t( 'eventDetail.cancel' ) }
              </Button>
            </Grid> : null}

          {(event.published && event.descriptor.state === "CANCELLED") ? <Grid size={4}>
              <Button key="btn_create"
                      style={ { marginTop: 15 } }
                      variant="outlined"
                      color="primary"
                      fullWidth={true}
                      size="small"
                      onClick={this.handleActiveEventClick}>
                <CloudDownloadIcon style={ { marginRight: 15 } }/> { t( 'eventDetail.activate' ) }
              </Button>
            </Grid> : null}
            
        
        </Grid>}/>
        <Divider style={ { marginTop: '20px' } } />
        <Grid>
         <Grid container spacing={ 3 }>
           <Grid size={3}>
             <EventTextField className={ classes.textField } field={ t( 'eventDetail.eventId' ) } value={ event.id } />
           </Grid>
           <Grid size={3}>
             <EventTextField className={ classes.textField } field={ t( 'eventDetail.modificationNumber' ) } value={ event.descriptor.modificationNumber } />
           </Grid>
           <Grid size={3}>
             <EventTextField className={ classes.textField } field={ t( 'eventDetail.oadrProfile' ) } value={ event.descriptor.oadrProfile } />
           </Grid>
           <Grid size={1}>
             <EventTextField className={ classes.textField } field={ t( 'eventDetail.priority' ) } value={ event.descriptor.priority } />
           </Grid>
            <Grid size={2}>
             <EventTextField className={ classes.textField } field={ t( 'eventDetail.state' ) } value={ event.descriptor.state } />
           </Grid>

         </Grid>

         <Grid container spacing={ 3 }>
           <Grid size={3}>
             <EventTextField className={ classes.textField } field={ t( 'eventDetail.createdDatetime' ) } 
             value={ createdDatetime.date + " " +createdDatetime.time + " " + createdDatetime.tz } />
           </Grid>
           <Grid size={3}>
             <EventTextField className={ classes.textField } field={ t( 'eventDetail.lastUpdateDatetime' ) } 
             value={ lastUpdateDatetime.date + " " +lastUpdateDatetime.time + " " + lastUpdateDatetime.tz } />
           </Grid>
            <Grid size={3}>
               <EventTextField className={ classes.textField } field={ t( 'eventDetail.vtnComment' ) } value={ event.descriptor.vtnComment } />
             </Grid>
             <Grid size={1}>
               <EventTextField className={ classes.textField } field={ t( 'eventDetail.isTestEvent' ) } value={ event.descriptor.testEvent } />
             </Grid>
             <Grid size={2}>
               <EventTextField className={ classes.textField } field={ t( 'eventDetail.responseRequired' ) } value={ event.descriptor.responseRequired } />
             </Grid>
         </Grid>

         
         
      
       </Grid>
      </div>
    );
  }
}

export default EventDetailDescriptor;
