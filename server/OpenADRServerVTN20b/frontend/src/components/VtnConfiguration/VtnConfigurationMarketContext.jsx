import React from 'react';

import Button from '@mui/material/Button';

// 카드 나열용. GridList -> ImageList 로 바뀌며 모양이 달라져서 예전 모양을 옮긴 컴포넌트를 쓴다
import CardList from '../common/CardList';





// material-ui-color-picker 는 MUI 3 과 react-color 에 묶여 있어서 자체 컴포넌트로 바꿨다
import ColorPicker from '../common/ColorPicker'



import TextField from '@mui/material/TextField';
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';





import Divider from '@mui/material/Divider';

import Grid from '@mui/material/Grid';

import { VtnConfigurationMarketContextCard } from '../common/VtnConfigurationCard'




export class VtnConfigurationMarketContext extends React.Component {
  constructor( props ) {
    super( props );

    this.state = {}
    this.state.name = ''
    this.state.description = ''
    this.state.color = '';
    this.state.editMode = false;


  }


  handleCreateMarketContextNameChange = (event) => {
    this.setState( {
      name: event.target.value
    } );
  };

  handleCreateMarketContextDescriptionChange = (event) => {
    this.setState( {
      description: event.target.value
    } );
  };

  handleCreateMarketContextColorChange = (color) => {
    this.setState( {
      color: color
    } );
  };

  handleCancelMarketContextButtonClick = () => {
    this.setState( {
      name: '',
      description: '',
      color: '',
      editMode: false
    } )
  }

  handleCreateMarketContextButtonClick = (event) => {
    event.preventDefault();
    var dto = {
      name: this.state.name,
      description: this.state.description,
      color: this.state.color
    }
    if ( !this.state.editMode ) {
      this.props.createMarketContext( dto )
    } else {
      this.props.updateMarketContext( dto )
    }


    this.setState( {
      name: '',
      description: '',
      color: '',
      editMode: false
    } )
  }

  handleDeleteMarketContext = (id) => {
    var that = this;
    return function ( event ) {
      event.preventDefault();
      that.props.deleteMarketContext( id );
    }

  }

  handleEditMarketContext = (context) => {
    var that = this;
    return function ( event ) {
      event.preventDefault();
      that.setState( {
        editMode: true,
        name: context.name,
        description: context.description,
        color: context.color
      } )
    }
  }



  render() {
    const {classes, marketContext} = this.props;
    var view = [];

    for (var i in marketContext) {
      var context = marketContext[ i ];

      view.push(

        <VtnConfigurationMarketContextCard key={ 'marketcontext_card_' + context.id }
                                           classes={ classes }
                                           context={ context }
                                           handleDeleteMarketContext={ this.handleDeleteMarketContext( context.id ) }
                                           handleEditMarketContext={ this.handleEditMarketContext( context ) } />
      );
    }

    var marginTop = 13;



    return (
      <div className={ classes.root }>
        <form >
          <Grid container spacing={ 1 }>
            <Grid container>
                  
              <Grid size={3}>
                <TextField
                  label="Name"
                  value={ this.state.name }
                  className={ classes.textField }
                  onChange={ this.handleCreateMarketContextNameChange }
                  disabled={ this.state.editMode }
                  style={{width:"95%"}}
                  slotProps={{
                    input: {style:{marginTop:24}},

                    inputLabel: {
                     shrink: true,
                   }
                  }} />
              </Grid>
              <Grid size={5}>
                <TextField
                  label="Description"
                  value={ this.state.description }
                  className={ classes.textField }
                  style={{width:"95%"}}
                  onChange={ this.handleCreateMarketContextDescriptionChange }
                  fullWidth={ true }
                  slotProps={{
                    input: {style:{marginTop:24}},

                    inputLabel: {
                       shrink: true,
                     }
                  }} />
              </Grid>
              <Grid size={2}>
                {/* 예전 TextFieldProps 로 넘기던 className, style 은 직접 받는다.
                    InputProps 의 marginTop:24 는 옛 패키지가 라벨 자리를 못 잡아서 넣은 보정이라 뺐다 */}
                <ColorPicker label="Color"
                             value={ this.state.color }
                             className={ classes.textField }
                             style={ { width: "95%" } }
                             onChange={ this.handleCreateMarketContextColorChange } />
              </Grid>
              <Grid size={1}>
                

                {(this.state.editMode) ? <Button key="vtn_cancel"
                          variant="outlined"
                          color="secondary"
                          size="small"
                          className={ classes.button }
                          style={ { marginTop } }
                          onClick={ this.handleCancelMarketContextButtonClick }>
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
                                onClick={ this.handleCreateMarketContextButtonClick }>
                          <AddIcon /> Save
                        </Button> : null}

                {(!this.state.editMode) ? <Button key="btn_create"
                              variant="outlined"
                              color="primary"
                              size="small"
                              className={ classes.button }
                              style={ { marginTop } }
                              fullWidth={ true } 
                              onClick={ this.handleCreateMarketContextButtonClick }>
                        <AddIcon />New
                      </Button>: null}

              </Grid>


              
            </Grid>
          </Grid>
        </form>
        <div >
          <Divider style={ { marginBottom: '20px', marginTop: '20px' } } />
          <CardList>
            { view }
          </CardList>
        </div>
      </div>
    );
  }
}

export default VtnConfigurationMarketContext;
