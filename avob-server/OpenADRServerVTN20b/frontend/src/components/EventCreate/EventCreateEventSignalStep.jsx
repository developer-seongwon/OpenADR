import React from 'react';
import { t } from '../../i18n';


import Grid from '@mui/material/Grid';

import Divider from '@mui/material/Divider';

import Button from '@mui/material/Button';
import AddIcon from '@mui/icons-material/Add';

import {EventSignalPanel} from '../common/EventSignalPanel'


export class EventCreateEventSignalStep extends React.Component {

  constructor( props ) {
    super( props );
    this.state = {};
  }

  handleAddSignalClick = () => {
    var eventSignal = this.props.eventSignal;
    eventSignal.push({
        signalId: "",
        signalName: "",
        signalType: "",
        unitType: "",
        intervals: [],
        currentValue: "",
      });
    this.props.onChange(eventSignal);
  }

  handleEventSignalChange = (index) => (signal) => {
    var eventSignal = this.props.eventSignal;
    eventSignal[index] = signal;
    this.props.onChange(eventSignal);
  }

  handleRemoveEventSignalChange = (index) => () => {
    var eventSignal = this.props.eventSignal;
    eventSignal.splice(index, 1)
    this.props.onChange(eventSignal);
  }




  render() {
    const {classes, hasError, eventSignal, activePeriod} = this.props;
    // 구간 표에 구간마다 활성 기간 안의 시작, 끝을 보여 주려고 넘긴다. 활성 기간 길이는 분이다(활성 기간 단계)
    var activeStart = activePeriod ? activePeriod.start : null;
    var activeMinutes = activePeriod ? activePeriod.duration : null;
    var that = this;
    return (
      <div>

        <Grid container
              style={ { marginTop: 20 } }
              spacing={ 3 }>
          <Grid size={2} />
          <Grid size={8}>
            {eventSignal.map((signal, index) => {
              return (
                <div key={"signal_panel_"+index}>
                  {(index !== 0) ? <Grid container
                        style={ { marginTop: 20, marginBottom: 20 } }
                        spacing={ 3 }>
                    <Grid size={2} />
                    <Grid size={8}>
                      <Divider />
                    </Grid>
                    <Grid size={2} />
                  </Grid> : null}

                  <EventSignalPanel 
                    classes={classes} eventSignal={signal} hasError={hasError} 
                      activeStart={activeStart} activeMinutes={activeMinutes}
                      timezone={activePeriod ? activePeriod.timezone : null}
                      onChange={that.handleEventSignalChange(index)}
                      onRemove={that.handleRemoveEventSignalChange(index)}
                      canBeRemoved={eventSignal.length >0}/>

                </div>
              );
            })}
          </Grid>
          <Grid size={2} />
        </Grid>

        <Grid container
              style={ { marginTop: 20 } }
              spacing={ 3 }>
          <Grid size={2} />
          <Grid size={8}>
            <Divider />
          </Grid>
          <Grid size={2} />
        </Grid>
        <Grid container
              style={ { marginTop: 20 } }
              spacing={ 3 }>
          <Grid size={2} />
          <Grid size={8}>
            <Button key="btn_create"
                            variant="outlined"
                            color="primary"
                            size="small"
                            className={ classes.button }
                            onClick={ this.handleAddSignalClick }>
                      <AddIcon />{ t( 'event.addNewSignal' ) }
                    </Button>
          </Grid>
          <Grid size={2} />
        </Grid>

      </div>
    );
  }
}

export default EventCreateEventSignalStep;
