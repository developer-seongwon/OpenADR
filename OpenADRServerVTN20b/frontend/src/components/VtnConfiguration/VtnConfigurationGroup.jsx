import React from 'react';

import Button from '@mui/material/Button';

// 카드 나열용. GridList -> ImageList 로 바뀌며 모양이 달라져서 예전 모양을 옮긴 컴포넌트를 쓴다
import CardList from '../common/CardList';









import TextField from '@mui/material/TextField';
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';







import Divider from '@mui/material/Divider';

import Grid from '@mui/material/Grid';











import { VtnConfigurationGroupCard } from '../common/VtnConfigurationCard'




export class VtnConfigurationGroup extends React.Component {
  constructor( props ) {
    super( props );

    this.state = {}
    this.state.name = ''
    this.state.description = ''
    this.state.editMode = false;


  }


  handleGroupNameChange = (event) => {
    this.setState( {
      name: event.target.value
    } );
  };

  handleGroupDescriptionChange = (event) => {
    this.setState( {
      description: event.target.value
    } );
  };

  handleCancelGroupButtonClick = () => {
    this.setState( {
      name: '',
      description: '',
      color: '',
      editMode: false
    } )
  }

  handleSaveGroupButtonClick = (event) => {
    event.preventDefault();
    var dto = {
      name: this.state.name,
      description: this.state.description,
    }
    if ( !this.state.editMode ) {
      this.props.createGroup( dto )
    } else {
      this.props.updateGroup( dto )
    }


    this.setState( {
      name: '',
      description: '',
      editMode: false
    } )
  }

  handleDeleteGroup = (id) => {
    var that = this;
    return function ( event ) {
      event.preventDefault();
      that.props.deleteGroup( id );
    }

  }

  handleEditGroup = (context) => {
    var that = this;
    return function ( event ) {
      event.preventDefault();
      that.setState( {
        editMode: true,
        name: context.name,
        description: context.description || "",
        color: context.color
      } )
    }
  }



  render() {
    const {classes, group} = this.props;
    var view = [];

    for (var i in group) {
      var g = group[ i ];

      view.push(

        <VtnConfigurationGroupCard key={ 'group_card_' + g.id }
                                   classes={ classes }
                                   group={ g }
                                   handleDeleteGroup={ this.handleDeleteGroup( g.id ) }
                                   handleEditGroup={ this.handleEditGroup( g ) } />
      );
    }

    var marginTop = 13;

    return (
      <div>
        <form className={ classes.root }>
          <Grid container spacing={ 1 }>
            <Grid container>
              <Grid size={3}>
                <TextField
                  label="Name"
                  value={ this.state.name }
                  className={ classes.textField }
                  onChange={ this.handleGroupNameChange }
                  disabled={ this.state.editMode }
                  style={{width:"95%"}}
                  slotProps={{
                    input: {style:{marginTop:24}},

                    inputLabel: {
                    shrink: true,
                  }
                  }} />
              </Grid>
              <Grid size={7}>
                <TextField
                  label="Description"
                  value={ this.state.description }
                  className={ classes.textField }
                  onChange={ this.handleGroupDescriptionChange }
                  style={{width:"95%"}}
                  slotProps={{
                    input: {style:{marginTop:24, }},

                    inputLabel: {
                    shrink: true,
                  }
                  }} />
              </Grid>
              <Grid size={1}>
                

                {(this.state.editMode) ? <Button key="vtn_cancel"
                          variant="outlined"
                          color="secondary"
                          size="small"
                          className={ classes.button }
                          style={ { marginTop } }
                          onClick={ this.handleCancelGroupButtonClick }>
                    <CloseIcon />
                  </Button>: null}      
              </Grid>
              <Grid size={1}>
                {(this.state.editMode) ? <Button key="btn_save"
                                variant="outlined"
                                color="primary"
                                size="small"
                                className={ classes.button }
                                style={ { marginTop } }
                                fullWidth={ true } 
                                onClick={ this.handleCreateGroupButtonClick }>
                          <AddIcon /> Save
                        </Button> : null}

                {(!this.state.editMode) ? <Button key="btn_create"
                              variant="outlined"
                              color="primary"
                              size="small"
                              className={ classes.button }
                              style={ { marginTop } }
                              fullWidth={ true } 
                              onClick={ this.handleCreateGroupButtonClick }>
                        <AddIcon />New
                      </Button>: null}

              </Grid>
            </Grid>
          </Grid>
        </form>
        <div>
          <Divider style={ { marginBottom: '20px', marginTop: '20px' } } />
          <CardList>
            { view }
          </CardList>
        </div>
      </div>
    );
  }
}

export default VtnConfigurationGroup;
