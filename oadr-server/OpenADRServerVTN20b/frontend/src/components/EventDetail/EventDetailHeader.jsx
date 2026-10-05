import React from 'react';
import { t } from '../../i18n';






import { VtnConfigurationEventCard } from '../common/VtnConfigurationCard'






import Grid from '@mui/material/Grid';


import Typography from '@mui/material/Typography';



















import SnackbarContent from '@mui/material/SnackbarContent';
import DoneIcon from '@mui/icons-material/Done';
import CloseIcon from '@mui/icons-material/Close';

import { amber } from '@mui/material/colors';


export class EventDetailHeader extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {}
  }


  render() {
    const {classes, event} = this.props;


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

    var ErrorSnackbar = (props) => {
      return (
        <SnackbarContent className={ classes.error }
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

    var WarningSnackbar = (props) => {
      return (
        <SnackbarContent className={ classes.success }
                         style={ { maxWidth: 'none', paddingTop:0, paddingBottom:0, backgroundColor:amber[700] } }
                         message={ 
                            <Grid container direction="row" sx={{
                              alignItems: "center"
                            }}>
                              <Grid>
                                <CloseIcon style={ { width: 20, height: 20, marginRight: 20, color:"#fff" } }/>
                              </Grid>
                              <Grid>
                                 { props.message }
                              </Grid>
                            </Grid>
                       } />
      );
    }


    var statePanel = null;
    var actionPanel = null;

    if(event.published){
      if ( event.descriptor.state === "ACTIVE") {
      
        statePanel = <SuccessSnackbar  message={ <Typography component="span" style={ { color:"#fff" } }>
                                 <strong>{ t( 'eventDetail.state.ACTIVE' ) }</strong>
                               </Typography> } />

      } else if ( event.descriptor.state === "CANCELLED" ) {
        statePanel = <ErrorSnackbar  message={ <Typography component="span" style={ { color:"#fff" } }>
                                 <strong>{ t( 'eventDetail.state.CANCELLED' ) }</strong>
                               </Typography> } />

      } 

    }
    else {
      // 아직 게시하지 않은 이벤트다. 예전에는 상태값(ACTIVE)만 찍혀서 게시된 것처럼 보였다. VEN 은 게시해야 받는다
      statePanel = <WarningSnackbar  message={ <Typography component="span" style={ { color:"#fff" } }>
         <strong>{ t( 'eventStatus.notPublished' ) }</strong>
       </Typography> } />
    }



    actionPanel = this.props.actions;

    return (
      <Grid container>
        <Grid container spacing={ 3 }>
          <Grid
            size={{
              lg: 4,
              md: 6
            }}>
            <VtnConfigurationEventCard key={ 'ven_card_' }
                                     classes={ classes }
                                     event={ event } />
          </Grid>
          <Grid
            size={{
              lg: 8,
              md: 6
            }}>


            <Grid container>
              <Grid container spacing={ 3 }>
                <Grid size={4}>
                  { statePanel }
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

export default EventDetailHeader;
