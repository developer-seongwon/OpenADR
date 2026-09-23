import React from 'react';





import Grid from '@mui/material/Grid';



import Button from '@mui/material/Button';
import AddIcon from '@mui/icons-material/Add';





import { history } from '../../store/configureStore';



import FilterPanel from '../common/FilterPanel' 


export class EventHeader extends React.Component {

  handleCreateEventClick = () => {
    history.push( '/event/create' );
  }

  render() {
    const {classes, marketContext, ven} = this.props;
    return (
      <div>
        <Grid container spacing={ 1 }>
          <Grid container>            
            <Grid size={11}>
              <FilterPanel classes={classes} type="EVENT" hasFilter={{marketContext:true, ven: true, eventStatus:true}} 
                marketContext={marketContext}
                filter={this.props.filters}
                onFilterChange={this.props.onFilterChange}
                ven={ven}
                onVenSuggestionsFetchRequested={this.props.onVenSuggestionsFetchRequested}
                onVenSuggestionsClearRequested={this.props.onVenSuggestionsClearRequested}
                onVenSuggestionsSelect={this.props.onVenSuggestionsSelect}
                />
            </Grid>
            <Grid size={1}>
             <Button key="btn_create"
                      variant="outlined"
                      color="primary"
                      size="small"
                      fullWidth={ true } 
                      className={ classes.button }
                      onClick={ this.handleCreateEventClick }>
                <AddIcon />New
              </Button>
            </Grid>
           
          </Grid>
        </Grid>

      </div>
    );
  }
}

export default EventHeader;

