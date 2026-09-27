import React from 'react';






import { VtnConfigurationVenCard } from '../common/VtnConfigurationCard'







import Grid from '@mui/material/Grid';


import Typography from '@mui/material/Typography';



















import SnackbarContent from '@mui/material/SnackbarContent';
import DoneIcon from '@mui/icons-material/Done';
import CloseIcon from '@mui/icons-material/Close';



export class VenDetailHeader extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {}
  }


  render() {
    const {classes, ven} = this.props;


    var SuccessSnackbar = (props) => {
      return (
        <SnackbarContent className={ classes.success }
                         style={ { maxWidth: 'none', paddingTop:0, paddingBottom:0 } }
                         message={ <Grid container direction="row" sx={{
                           alignItems: "center"
                         }}>
                              <Grid>
                                <DoneIcon style={ { width: 20, height: 20, marginRight: 20, color:"#fff" } }/>
                              </Grid>
                              <Grid>
                                 { props.message }
                              </Grid>
                            </Grid>} />
      );
    }

    var DefaultSnackbar = (props) => {
      return (
        <SnackbarContent className={ classes.success }
                         style={ { maxWidth: 'none', paddingTop:0, paddingBottom:0, backgroundColor:"#fafafa" } }
                         message={ 
                            <Grid container direction="row" sx={{
                              alignItems: "center"
                            }}>
                              <Grid>
                                <CloseIcon style={ { width: 20, height: 20, marginRight: 20, color:"#000" } }/>
                              </Grid>
                              <Grid>
                                 { props.message }
                              </Grid>
                            </Grid>
                       } />
      );
    }


    var registrationPanel = null;
    var actionPanel = null;


    if ( ven.registrationId == null ) {
      // MUI 3 의 Typography 는 글자색을 text.primary(검정)로 직접 칠했고 MUI 5 부터는 부모 색을 물려받는다.
      // SnackbarContent 의 글자색이 흰색이라 연회색 바탕 위에서 글자가 안 보였다. 옆 아이콘처럼 검정을 준다
      registrationPanel = <DefaultSnackbar  message={ <Typography component="span" style={ { color:"#000" } }>
                               <strong>Not Registered</strong>
                             </Typography> } />

    } else {
      registrationPanel = <SuccessSnackbar  message={ <Typography component="span" style={ { color:"#fff" } }>
                               <strong>Registered</strong>
                             </Typography> } />
      actionPanel = this.props.actions;

    }

    return (
      <Grid container>
        <Grid container spacing={ 3 }>
          <Grid
            size={{
              lg: 4,
              md: 6
            }}>
            <VtnConfigurationVenCard key={ 'ven_card_' }
                                     classes={ classes }
                                     ven={ ven } />
          </Grid>
          <Grid
            size={{
              lg: 8,
              md: 6
            }}>


            <Grid container>
              <Grid container>
                <Grid size={4}>
                  { registrationPanel }
                </Grid>
  
              </Grid>
              {actionPanel}
            

            </Grid>
          </Grid>
        </Grid>
      </Grid>
    );
  }
}

export default VenDetailHeader;
