import React from 'react';









import Grid from '@mui/material/Grid';


























import {EventTargetPanel} from '../common/EventTargetPanel'



export class EventCreateEventTarget extends React.Component {

  render() {
    const {classes, eventTarget, group, ven} = this.props;

    return (
      <Grid container
            spacing={ 1 }
            sx={{
              justifyContent: "center"
            }}>

        <Grid container spacing={ 3 }>
          <Grid size={2} />
            <Grid size={8}>
              <EventTargetPanel classes={classes} eventTarget={eventTarget} group={group} onChange={this.props.onChange}
              ven={ven}
              onVenSuggestionsFetchRequested={this.props.onVenSuggestionsFetchRequested}
              onVenSuggestionsClearRequested={this.props.onVenSuggestionsClearRequested}
              onVenSuggestionsSelect={this.props.onVenSuggestionsSelect}/>
            </Grid>

            
          <Grid size={2} />
        </Grid>

      </Grid>
    );
  }
}

export default EventCreateEventTarget;
