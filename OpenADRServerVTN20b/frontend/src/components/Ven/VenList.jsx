import React from 'react';

import Divider from '@mui/material/Divider';

import Button from '@mui/material/Button';
import AddIcon from '@mui/icons-material/Add';

// 카드 나열용. GridList -> ImageList 로 바뀌며 모양이 달라져서 예전 모양을 옮긴 컴포넌트를 쓴다
import CardList from '../common/CardList';

import { VtnConfigurationVenCard } from '../common/VtnConfigurationCard'

import { history } from '../../store/configureStore';
import Grid from '@mui/material/Grid';


import FilterPanel from '../common/FilterPanel' 

export class VenList extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {}
  }

  handleDeleteVen = (username) => {
    var that = this;
    return function ( event ) {
      event.preventDefault();
      that.props.deleteVen( username )

    }

  }

  handleEditVen = (username) => {
    return function ( event ) {
      event.preventDefault();
      history.push( '/ven/detail/' + username )
    }
  }

  handleCreateVENClick = () => {
    history.push( '/ven/create' )
  }

  render() {
    const {classes, ven, marketContext, group, event} = this.props;
    var view = [];

    for (var i in ven) {
      var v = ven[ i ];
      view.push(

        <VtnConfigurationVenCard key={ 'ven_card_' + v.username }
                                 classes={ classes }
                                 ven={ v }
                                 handleDeleteVen={ this.handleDeleteVen( v.username ) }
                                 handleEditVen={ this.handleEditVen( v.username ) } />
      );
    }
    return (
      <div className={ classes.root }>
        <Grid container spacing={ 1 }>
            <Grid container
                  >

              <Grid size={11}>
                <FilterPanel classes={classes}  type="VEN" hasFilter={{group:true, marketContext:true, venStatus:true, event:true}} 
                  group={group}
                  marketContext={marketContext}
                  filter={this.props.filters}
                  onFilterChange={this.props.onFilterChange}

                  event={event}
                  onEventSuggestionsFetchRequested={this.props.onEventSuggestionsFetchRequested}
                  onEventSuggestionsClearRequested={this.props.onEventSuggestionsClearRequested}
                  onEventSuggestionsSelect={this.props.onEventSuggestionsSelect}/>
              </Grid>
              <Grid size={1}>
               <Button key="btn_create"
                        variant="outlined"
                        color="primary"
                        size="small"
                        className={ classes.button }
                        fullWidth={ true } 
                        onClick={ this.handleCreateVENClick }>
                  <AddIcon />New
                </Button>
              </Grid>
             
            </Grid>
          </Grid>

        <Divider style={ { marginBottom: '20px', marginTop: '20px' } } />
        <CardList>
          { view }
        </CardList>
      </div>
    );
  }
}

export default VenList;
