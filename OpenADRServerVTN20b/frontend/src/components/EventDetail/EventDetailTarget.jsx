import React from 'react';


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
                <CloudDownloadIcon style={ { marginRight: 15 } }/> PUBLISH
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
                <CloudDownloadIcon style={ { marginRight: 15 } }/> UPDATE
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
                <CloudDownloadIcon style={ { marginRight: 15 } }/> UPDATE AND PUBLISH
              </Button>
            </Grid> : null}
        
        </Grid>}/>
        <Divider style={ { marginTop: '20px', marginBottom:20 } } />

        <EventTargetPanel classes={classes} eventTarget={copyTargets} group={group} onChange={this.props.updateCopyTargets}
         ven={ven}
         onVenSuggestionsFetchRequested={this.props.onVenSuggestionsFetchRequested}
         onVenSuggestionsClearRequested={this.props.onVenSuggestionsClearRequested}
         onVenSuggestionsSelect={this.props.onVenSuggestionsSelect}/>

      </div>
    );
  }
}

export default EventDetailTarget;
