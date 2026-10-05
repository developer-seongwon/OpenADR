import React from 'react';
import { t } from '../../i18n';









import Button from '@mui/material/Button';
import AddIcon from '@mui/icons-material/Add';
import CloudDownloadIcon from '@mui/icons-material/CloudDownload';




import EventDetailHeader from './EventDetailHeader'
import Divider from '@mui/material/Divider';


import Grid from '@mui/material/Grid';







import {EventSignalPanel} from '../common/EventSignalPanel'
import { durationToMinutes } from '../../utils/time';


export class EventDetailSignal extends React.Component {




  handleAddSignalClick = () => {
    this.props.addCopySignals();
  }

  handleEventSignalChange = (index) => (newSignal) => {
    this.props.updateCopySignals(index, newSignal);
  }

  handleRemoveEventSignalChange = (index) => () => {
    this.props.removeCopySignals(index);
  }

  handlePublishEventClick = () => {
    this.props.publishEvent(this.props.event.id);
  }


  render() {
    const {classes, event, copySignals, editMode} = this.props;
    // 구간 표에 구간마다 활성 기간 안의 시작, 끝을 보여 주려고 넘긴다. 서버의 활성 기간 길이는 XML 기간이라 분으로 바꾼다
    var activeStart = (event && event.activePeriod) ? event.activePeriod.start : null;
    var activeMinutes = (event && event.activePeriod) ? durationToMinutes(event.activePeriod.duration) : null;
    var that = this;
    var hasError = false;

    return (
      <div className={ classes.root } >
        <EventDetailHeader classes={classes} event={event} actions={<Grid container spacing={ 3 }>

          {(!event.published) ? <Grid size={4}>
              <Button key="btn_create"
                      style={ { marginTop: 15 } }
                      variant="outlined"
                      color="primary"
                      fullWidth={true}
                      size="small"
                      onClick={this.handlePublishEventClick}>
                <CloudDownloadIcon style={ { marginRight: 15 } }/> { t( 'eventDetail.publish' ) }
              </Button>
            </Grid> : null}
            

          {(editMode) ? <Grid size={4}>
              <Button key="btn_create"
                      style={ { marginTop: 15 } }
                      variant="outlined"
                      color="primary"
                      fullWidth={true}
                      size="small"
                      onClick={() => {this.props.updateEvent(false)}}>
                <CloudDownloadIcon style={ { marginRight: 15 } }/> { t( 'eventDetail.update' ) }
              </Button>
            </Grid> : null}

          {(editMode) ? <Grid size={4}>
              <Button key="btn_create"
                      style={ { marginTop: 15 } }
                      variant="outlined"
                      color="secondary"
                      fullWidth={true}
                      size="small"
                      onClick={() => {this.props.updateEvent(true)}}>
                <CloudDownloadIcon style={ { marginRight: 15 } }/> { t( 'eventDetail.updateAndPublish' ) }
              </Button>
            </Grid> : null}
        
        </Grid>}/>

        <Divider style={ { marginTop: '20px', marginBottom:20 } } />
        {copySignals.map((signal, index) => {
           return (
             <div key={"signal_panel_"+index}>
               {(index !==0) ? <Grid container
                     style={ { marginTop: 20, marginBottom: 20 } }
                     spacing={ 3 }>
                 <Grid size={12}>
                   <Divider />
                 </Grid>
               </Grid> : null}

               <EventSignalPanel 
                 classes={classes} eventSignal={signal} hasError={hasError} 
                   activeStart={activeStart} activeMinutes={activeMinutes}
                   onChange={that.handleEventSignalChange(index)}
                   onRemove={that.handleRemoveEventSignalChange(index)}
                   canBeRemoved={copySignals.length >0}/>

             </div>
           );
         })}

        <Divider style={ { marginTop: 30} } />
        <Grid container
              style={ { marginTop: 20 } }
              spacing={ 3 }>
          <Grid size={12}>
            <Button key="btn_create"
                            variant="outlined"
                            color="primary"
                            size="small"
                            className={ classes.button }
                            onClick={ this.handleAddSignalClick }>
                      <AddIcon />{ t( 'event.addNewSignal' ) }
                    </Button>
          </Grid>
        </Grid>
      </div>
    );
  }
}

export default EventDetailSignal;
