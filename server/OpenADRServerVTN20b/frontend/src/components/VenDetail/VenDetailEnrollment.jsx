import React from 'react';






import {  VtnConfigurationMarketContextCard } from '../common/VtnConfigurationCard'
import { MarketContextSelectDialog } from '../common/VtnconfigurationDialog'






import Grid from '@mui/material/Grid';




import Divider from '@mui/material/Divider';


import ImageList from '@mui/material/ImageList';
import ImageListItem from '@mui/material/ImageListItem';


import IconButton from '@mui/material/IconButton';



// material-ui-chip-input 은 MUI 3 전용이라 같은 모양의 자체 컴포넌트로 바꿨다
import ChipInput from '../common/ChipInput'

import Button from '@mui/material/Button';
import AddIcon from '@mui/icons-material/Add';

import SearchIcon from '@mui/icons-material/Search';







import VenDetailHeader from './VenDetailHeader'














var MarketContextGridList = (props) => {
  return (
  <div className={ props.classes.root }>
    {/* MUI 5 에서 GridList 가 ImageList 로 바뀌면서 cellHeight 는 rowHeight, spacing 은 gap 이 됐다 */}
    <ImageList className={ props.classes.gridList }
              cols={ 3 }
              gap={ 0 }
              rowHeight="auto">
      { props.marketContext.map( context => (
          <ImageListItem key={ context.id } className={ props.classes.tile }>
            <VtnConfigurationMarketContextCard classes={ props.classes }
                                               context={ context }
                                               handleRemoveVenMarketContext={ props.handleRemoveVenMarketContext( context ) } />
          </ImageListItem>
        ) ) }
    </ImageList>
  </div>
  );
}

export class VenDetailEnrollment extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {}
     this.state.marketContextSelectDialogOpen = false;
  }

  handleMarketContextSelectOpen = () => {
    this.setState( {
      marketContextSelectDialogOpen: true
    } );
  }

  handleMarketContextSelectClose = (context) => {
    if ( context ) {
      this.props.addVenMarketContext( this.props.ven.username, context.id )
    }
    this.setState( {
      marketContextSelectDialogOpen: false
    } );
  }

  handleRemoveVenMarketContext = (context) => {
    return () => {
      console.log( context )
      this.props.removeVenMarketContext( this.props.ven.username, context.id )
    }
  }

  render() {
    const {classes, ven, marketContext, venMarketContext} = this.props;

    var notSubscribedMarketContext = [];
    var venMarketContextId = [];
    for (var i in venMarketContext) {
      venMarketContextId.push( venMarketContext[ i ].id );
    }
    for (var j in marketContext) {
      if ( venMarketContextId.indexOf( marketContext[ j ].id ) === -1 ) {
        notSubscribedMarketContext.push( marketContext[ j ] )
      }
    }


    return (
      <div className={ classes.root } >
        <VenDetailHeader classes={classes} ven={ven} actions={[
   
        ]
        }/>
        { /* MarketContext Row */ }
        <Divider style={ { marginTop: '20px' } } />
        <Grid container >
          <Grid container>
            <Grid size={8}>
              <ChipInput label="Filters"
                         placeholder="Filters"
                         value={ this.state.filter }
                         onAdd={ this.handleAddChip }
                         onDelete={ this.handleDeleteChip }
                         fullWidth={ true } />
            </Grid>
            <Grid size={1}>
              <IconButton className={ classes.iconButton } aria-label="Search" size="large">
                <SearchIcon />
              </IconButton>
            </Grid>
            <Grid size={3}>
              <Button key="btn_create"
                      style={ { marginTop: 15 } }
                      variant="outlined"
                      color="primary"
                      size="small"
                      fullWidth={true}
                      className={ classes.button }
                      onClick={ this.handleMarketContextSelectOpen }>
                <AddIcon />Subscribe to a Market Context
              </Button>
              <MarketContextSelectDialog marketContext={ notSubscribedMarketContext }
                                         open={ this.state.marketContextSelectDialogOpen }
                                         close={ this.handleMarketContextSelectClose }
                                         title="Add VEN to Market Context" />
            </Grid>
          </Grid>
          <Grid container spacing={ 3 }>
            <Grid size={12}>
              <MarketContextGridList classes={ classes }
                                     marketContext={ venMarketContext }
                                     handleRemoveVenMarketContext={ this.handleRemoveVenMarketContext } />
            </Grid>
          </Grid>
        </Grid>

      </div>
    );
  }
}

export default VenDetailEnrollment;
