import React from 'react';






import { VtnConfigurationGroupCard } from '../common/VtnConfigurationCard'
import {  GroupSelectDialog } from '../common/VtnconfigurationDialog'






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















var GroupGridList = (props) => {
  return (
  <div className={ props.classes.root }>
    {/* MUI 5 에서 GridList 가 ImageList 로 바뀌면서 cellHeight 는 rowHeight, spacing 은 gap 이 됐다 */}
    <ImageList className={ props.classes.gridList }
              cols={ 3 }
              gap={ 0 }
              rowHeight="auto">
      { props.group.map( g => (
          <ImageListItem key={ g.id } className={ props.classes.tile }>
            <VtnConfigurationGroupCard classes={ props.classes }
                                       group={ g }
                                       handleRemoveVenGroup={ props.handleRemoveVenGroup( g ) } />
          </ImageListItem>
        ) ) }
    </ImageList>
  </div>
  );
}


export class VenDetailGroup extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {};
    this.state.groupSelectDialogOpen = false;
  }

  handleGroupSelectOpen = () => {
    this.setState( {
      groupSelectDialogOpen: true
    } );
  }

  handleGroupSelectClose = (group) => {
    console.log( group )
    if ( group ) {
      this.props.addVenGroup( this.props.ven.username, group.id )
    }
    this.setState( {
      groupSelectDialogOpen: false
    } );
  }



  handleRemoveVenGroup = (group) => {
    return () => {
      console.log( group )
      this.props.removeVenGroup( this.props.ven.username, group.id )
    }
  }


  render() {
    const {classes, ven,  group, venGroup} = this.props;

    var notAddedGroup = [];
    var venGroupId = [];
    for (var i in venGroup) {
      venGroupId.push( venGroup[ i ].id );
    }
    for (var j in group) {
      if ( venGroupId.indexOf( group[ j ].id ) === -1 ) {
        notAddedGroup.push( group[ j ] )
      }
    }

    return (
      <div className={ classes.root } >
        <VenDetailHeader classes={classes} ven={ven} actions={[
   
        ]
        }/>
        <Divider style={ { marginTop: '20px'} } />
        { /* Group Row */ }
        <Grid container>
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
                      onClick={ this.handleGroupSelectOpen }>
                <AddIcon />Add to a Group
              </Button>
              <GroupSelectDialog group={ notAddedGroup }
                                 open={ this.state.groupSelectDialogOpen }
                                 close={ this.handleGroupSelectClose }
                                 title="Add VEN to group:" />
            </Grid>
          </Grid>
          <Grid container spacing={ 3 }>
            <Grid size={12}>
              <GroupGridList classes={ classes }
                             group={ venGroup }
                             handleRemoveVenGroup={ this.handleRemoveVenGroup } />
            </Grid>
          </Grid>
        </Grid>

      </div>
    );
  }
}

export default VenDetailGroup;
