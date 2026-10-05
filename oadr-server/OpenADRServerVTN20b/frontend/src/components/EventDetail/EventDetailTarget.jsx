import React from 'react';
import { t } from '../../i18n';


import EventDetailHeader from './EventDetailHeader'
import Divider from '@mui/material/Divider';

import {EventTargetPanel} from '../common/EventTargetPanel'

import Grid from '@mui/material/Grid';
import Button from '@mui/material/Button';
import CloudDownloadIcon from '@mui/icons-material/CloudDownload';

export class EventDetailTarget extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {}


  }

  // 예전에는 이 메서드가 없어서 대상 탭의 게시 버튼을 눌러도 아무 일이 없었다
  handlePublishEventClick = () => {
    this.props.publishEvent(this.props.event.id);
  }

  render() {
   const {classes, event, copyTargets, group, editMode, ven} = this.props;
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
            

          {(editMode) ? <Grid size={4}>
              <Button key="btn_create"
                      style={ { marginTop: 15 } }
                      variant="outlined"
                      color="primary"
                      fullWidth={true}
                      size="small"
                      onClick={() => {this.props.updateEvent(false)}}>
                <CloudDownloadIcon style={ { marginRight: 15 } }/> { t( 'eventDetail.update' ) }
              </Button>
            </Grid> : null}

          {(editMode) ? <Grid size={4}>
              <Button key="btn_create"
                      style={ { marginTop: 15 } }
                      variant="outlined"
                      color="secondary"
                      fullWidth={true}
                      size="small"
                      onClick={() => {this.props.updateEvent(true)}}>
                <CloudDownloadIcon style={ { marginRight: 15 } }/> { t( 'eventDetail.updateAndPublish' ) }
              </Button>
            </Grid> : null}
        
        </Grid>}/>
        <Divider style={ { marginTop: '20px', marginBottom:20 } } />

        <EventTargetPanel classes={classes} eventTarget={copyTargets} group={group} onChange={this.props.updateCopyTargets}
         marketContext={event.descriptor ? event.descriptor.marketContext : null}
         ven={ven}
         onVenSuggestionsFetchRequested={this.props.onVenSuggestionsFetchRequested}
         onVenSuggestionsClearRequested={this.props.onVenSuggestionsClearRequested}/>

      </div>
    );
  }
}

export default EventDetailTarget;
