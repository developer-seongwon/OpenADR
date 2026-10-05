import React from 'react';
import { t } from '../../i18n';

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

// 새로 만들기 칸의 기본값. 처음 띄운 VTN 에는 MarketContext 가 없어서 이 값으로 바로 하나 만들 수 있게 둔다.
// 취소하거나 만든 뒤에도 이 값으로 돌아간다
const DEFAULT_NAME = 'OpenADR';
const DEFAULT_DESCRIPTION = 'OpenADR VTN 기본 MarketContext';

function emptyForm() {
  return {
    name: DEFAULT_NAME,
    description: DEFAULT_DESCRIPTION,
    color: '',
    editMode: false,
    // 이름 칸 아래에 보일 문구의 i18n 키. 없으면 null
    nameError: null,
  };
}




export class VtnConfigurationMarketContext extends React.Component {
  constructor( props ) {
    super( props );

    this.state = emptyForm();


  }


  handleCreateMarketContextNameChange = (event) => {
    this.setState( {
      name: event.target.value,
      nameError: null
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
    this.setState( emptyForm() )
  }

  hasMarketContext = (name) => {
    var marketContext = this.props.marketContext || [];
    for (var i in marketContext) {
      if ( marketContext[ i ].name === name ) {
        return true;
      }
    }
    return false;
  }

  // 이름이 MarketContext 의 키다. 예전에는 비어 있어도 그대로 보내서 이름 없는 MarketContext 가 생겼다.
  // 같은 이름은 서버가 406 으로 거절하는데 화면에는 아무것도 안 떠서, 새로 만들 때는 여기서 먼저 막는다.
  // 수정은 이름 칸이 잠겨 있고 그 이름으로 찾으므로 손대지 않고 보낸다
  handleCreateMarketContextButtonClick = (event) => {
    event.preventDefault();
    // 예전에 이름 없이 만들어진 MarketContext 를 수정하면 name 이 null 일 수 있다
    var name = this.state.name || '';
    if ( !this.state.editMode ) {
      name = name.trim();
    }
    if ( name.trim() === '' ) {
      this.setState( { nameError: 'vtnConfig.nameRequired' } );
      return;
    }
    if ( !this.state.editMode && this.hasMarketContext( name ) ) {
      this.setState( { nameError: 'vtnConfig.nameDuplicated' } );
      return;
    }
    var dto = {
      name: name,
      description: this.state.description,
      color: this.state.color
    }
    if ( !this.state.editMode ) {
      this.props.createMarketContext( dto )
    } else {
      this.props.updateMarketContext( dto )
    }

    this.setState( emptyForm() )
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
        color: context.color,
        nameError: null
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
                  
              {/* 예전 MUI 3 보정이던 입력칸 marginTop 24 를 뺐다. 라벨과 입력칸 사이 24px 는 눌러도 입력칸이 잡히지 않는
                  빈자리라, 처음에 칸이 비어 있으면 라벨 바로 밑을 눌러도 입력이 안 됐다. 색 칸(ColorPicker)은 이미 뺐다.
                  id 를 줘서 라벨을 눌러도 입력칸이 잡힌다 */}
              <Grid size={3}>
                <TextField
                  id="marketcontext_name"
                  required
                  label={ t( 'vtnConfig.name' ) }
                  value={ this.state.name }
                  className={ classes.textField }
                  onChange={ this.handleCreateMarketContextNameChange }
                  disabled={ this.state.editMode }
                  error={ this.state.nameError != null }
                  helperText={ this.state.nameError ? t( this.state.nameError ) : null }
                  style={{width:"95%"}}
                  slotProps={{
                    inputLabel: {
                     shrink: true,
                   }
                  }} />
              </Grid>
              <Grid size={5}>
                <TextField
                  id="marketcontext_description"
                  label={ t( 'vtnConfig.description' ) }
                  value={ this.state.description }
                  className={ classes.textField }
                  style={{width:"95%"}}
                  onChange={ this.handleCreateMarketContextDescriptionChange }
                  fullWidth={ true }
                  slotProps={{
                    inputLabel: {
                       shrink: true,
                     }
                  }} />
              </Grid>
              <Grid size={2}>
                {/* 예전 TextFieldProps 로 넘기던 className, style 은 직접 받는다.
                    InputProps 의 marginTop:24 는 옛 패키지가 라벨 자리를 못 잡아서 넣은 보정이라 뺐다 */}
                <ColorPicker label={ t( 'vtnConfig.color' ) }
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
                          <AddIcon /> { t( 'common.save' ) }
                        </Button> : null}

                {(!this.state.editMode) ? <Button key="btn_create"
                              variant="outlined"
                              color="primary"
                              size="small"
                              className={ classes.button }
                              style={ { marginTop } }
                              fullWidth={ true } 
                              onClick={ this.handleCreateMarketContextButtonClick }>
                        <AddIcon />{ t( 'common.new' ) }
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
