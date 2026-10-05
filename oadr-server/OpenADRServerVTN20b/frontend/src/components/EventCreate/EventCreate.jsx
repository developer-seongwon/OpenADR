import React from 'react';
import { t } from '../../i18n';




import Stepper from '@mui/material/Stepper';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';







import Paper from '@mui/material/Paper';
import Alert from '@mui/material/Alert';

import EventCreateDescriptorStep from './EventCreateDescriptorStep'
import EventCreateActivePeriodStep from './EventCreateActivePeriodStep'
import EventCreateEventSignalStep from './EventCreateEventSignalStep'
import EventCreateEventTarget from './EventCreateEventTarget'
import EventCreateConfirmationStep from './EventCreateConfirmationStep'

import {minutesToIntervalDuration, browserTimezone} from '../../utils/time'
import {numberOrNull, signalsToPayload, targetsToPayload} from '../../utils/eventPayload'





function getSteps() {
  return [ t( 'eventCreate.step.descriptor' ), t( 'eventCreate.step.activePeriod' ), t( 'eventCreate.step.signal' ),
    t( 'eventCreate.step.targets' ), t( 'eventCreate.step.confirmation' ) ];
}


export class EventCreate extends React.Component {
  constructor( props ) {
    super( props );
    var now = new Date()
    this.state = {
      activeStep: 0,
      descriptor: {
        // eventName: "",
        // timezone: "UTC",
        // priority:0,
        // responseRequired:"always",
        // testEvent: false,
        // vtnComment: "",
       
        // vtnComment: "",
        // marketContext: null
        oadrProfile: "OADR20B",
        priority:0,
        responseRequired:"ALWAYS",
        testEvent: false,
        vtnComment: "",
        // 예전에는 없는 이름(http://MarketContext1)이 들어 있어서 안 바꾸고 만들면 서버가 400 으로 거절했다.
        // 비워 두고 목록을 읽으면 첫 번째를 고른다(selectDefaultMarketContext)
        marketContext: ""
      },
      activePeriod: {
        // start: null,
        // duration: "",
        // 시작 시각을 고르고 보여 주는 시간대. 예전에는 UTC 로 적혀 있었지만 실제로는 쓰이지 않았다
        timezone: browserTimezone(),
        start: now.getTime(),
        duration: 120,
        notificationDuration: 120,
        rampUpDuration: 120,
        toleranceDuration: 120,
        recoveryDuration: 120,
      },
      eventSignal: [{
        // signalName: "",
        // signalType: "",
        // unitType: "",
        // signalInterval: [],
        // currentValue: "",
     
        intervals: [],
        currentValue: "97.5",
        signalName: "ENERGY_PRICE",
        signalType: "PRICE",
        unitType: "euro_per_kwh",
        
      }],
      // 예전에는 없는 VEN 지문이 대상으로 들어 있었다. 대상 단계에서 직접 고른다
      eventTarget: []
    };
  }

  componentDidMount() {
    this.selectDefaultMarketContext();
  }

  componentDidUpdate( prevProps ) {
    if ( prevProps.marketContext !== this.props.marketContext ) {
      this.selectDefaultMarketContext();
    }
  }

  // MarketContext 를 아직 안 골랐으면 목록의 첫 번째로 채운다. 보통 하나뿐이다(기본 OpenADR)
  selectDefaultMarketContext = () => {
    var list = this.props.marketContext;
    if ( this.state.descriptor.marketContext || !list || list.length === 0 ) {
      return;
    }
    this.setState( { descriptor: { ...this.state.descriptor, marketContext: list[ 0 ].name } } );
  }

  handleFinish = () => {
    this.handleCreateEvent();
  }

  handleNext = () => {
    const {activeStep} = this.state;
    if ( activeStep === getSteps().length - 1 ) {
      this.handleFinish();
    } else {
      this.setState( {
        activeStep: activeStep + 1,
      } );
    }

  };

  handleBack = () => {
    this.setState( state => ({
      activeStep: state.activeStep - 1,
    }) );
  };

  handleReset = () => {
    this.setState( {
      activeStep: 0,
    } );
  };

 

  handleCreateEvent = (needPublish) => {
    // 분을 XML 기간으로. 비워 둔 값은 null 이다.
    // 예전에는 빈 값("")도 그대로 바꿔서 "PTM" 을 보냈고 서버가 기간 형식이 아니라며 400 을 냈다(고급 항목을 끄면 넷 다 빈 값이 된다)
    var durationOrNull = (value) => {
      return (value == null || value === "") ? null : minutesToIntervalDuration(value);
    }
    var period = this.state.activePeriod;
    var activePeriod = {
      start: period.start,
      duration: minutesToIntervalDuration(period.duration),
      // 서버(DemandResponseEventDtoValidator)는 알림 기간과 허용 오차 기간을 꼭 받는다. 비우면 0분으로 보낸다
      notificationDuration: durationOrNull(period.notificationDuration) || "PT0M",
      toleranceDuration: durationOrNull(period.toleranceDuration) || "PT0M",
    }
    var rampUpDuration = durationOrNull(period.rampUpDuration);
    if (rampUpDuration) {
      activePeriod.rampUpDuration = rampUpDuration;
    }
    var recoveryDuration = durationOrNull(period.recoveryDuration);
    if (recoveryDuration) {
      activePeriod.recoveryDuration = recoveryDuration;
    }

    var dto = {
      // 이벤트 만들기 버튼은 인자 없이 부른다. 서버 DTO 의 published 는 boolean 이라 늘 true, false 로 보낸다
      published: needPublish === true,
      descriptor: {
        marketContext: this.state.descriptor.marketContext,
        priority: numberOrNull(this.state.descriptor.priority),
        responseRequired: this.state.descriptor.responseRequired,
        testEvent: this.state.descriptor.testEvent,
        state: "ACTIVE" ,
        oadrProfile: this.state.descriptor.oadrProfile,
      },

      activePeriod: activePeriod,

      // 신호와 대상은 서버 DTO 모양으로 바꿔 보낸다(utils/eventPayload). 이벤트 상세에서 고칠 때도 같은 것을 쓴다
      signals: signalsToPayload(this.state.eventSignal),
      targets: targetsToPayload(this.state.eventTarget),
    }
    this.props.createEvent( dto );
  }

  // 단계 화면들이 받은 객체를 고쳐서 그대로 넘긴다. 예전에는 setState(descriptor) 처럼 넘겨서
  // 필드가 state 맨 위에 흩어졌다(배열이면 "0", "1" 키). 제자리에 넣는다
  handleDescriptorChange = (descriptor) => {
    this.setState({descriptor: descriptor});
  }

  handleActivePeriodChange = (activePeriod) => {
    this.setState({activePeriod: activePeriod});
  }

  handleEventSignalChange = (eventSignal) => {
    this.setState({eventSignal: eventSignal});
  }

  handleEventTargetChange = (eventTarget) => {
    this.setState({eventTarget: eventTarget});
  }


  

  render() {
    const {classes, marketContext, group, ven, creating, createError} = this.props;
    const steps = getSteps();
    const {activeStep, descriptor, activePeriod, eventSignal, eventTarget, hasError} = this.state;
    var that = this;


    var getStepContent = ( step ) => {
      switch (step) {
        case 0:
          return <EventCreateDescriptorStep classes={classes} marketContext={marketContext} 
            descriptor={descriptor} onChange={this.handleDescriptorChange} hasError={hasError}/>
        case 1:
          return <EventCreateActivePeriodStep classes={classes} marketContext={marketContext} 
            activePeriod={activePeriod} onChange={this.handleActivePeriodChange} hasError={hasError}/>
        case 2:
          return <EventCreateEventSignalStep classes={classes} onChange={this.handleEventSignalChange}
            eventSignal={eventSignal} hasError={hasError} activePeriod={activePeriod}/>
        case 3:
          // VEN 찾기는 이벤트의 MarketContext 에 가입한 VEN 만 보이게 마켓 이름을 같이 넘긴다.
          // 가입 안 한 VEN 은 대상에 넣어도 서버가 연결하지 않아서 이벤트를 못 받는다
          return <EventCreateEventTarget classes={classes} group={group} eventTarget={eventTarget} 
            onChange={this.handleEventTargetChange}
            hasError={hasError}
            marketContext={descriptor.marketContext}
            ven={ven}
            onVenSuggestionsFetchRequested={(e) => this.props.onVenSuggestionsFetchRequested({ ...e, marketContext: descriptor.marketContext })}
            onVenSuggestionsClearRequested={this.props.onVenSuggestionsClearRequested}
            onVenSuggestionsSelect={this.props.onVenSuggestionsSelect}/>
        case 4:
          return <EventCreateConfirmationStep classes={classes} group={group} marketContext={marketContext} 
            descriptor={descriptor} activePeriod={activePeriod} eventTarget={eventTarget} eventSignal={eventSignal}/>
        default:
          return t( 'common.unknownStep' );
      }
    }

    

    function getSetValidation( step ) {
      switch (step) {
        case 0:
          return descriptor.priority !== null
            && Boolean(descriptor.marketContext)
            && descriptor.eventName !== "";

        case 1:
          return activePeriod.start !== null 
            && activePeriod.duration !== "";

        case 2:

          for(var i in eventSignal){
            if( eventSignal[i].signalName === "" 
              || eventSignal[i].signalType === "" 
              || (eventSignal[i].currentValue === "" 
              && (eventSignal[i].intervals || []).length === 0 )){
              return false;
            }
          }
          return true;
          
            
        case 3:
          return eventTarget.length > 0;
        case 4:
          return true;
        default:
          return false
      }
    }

    function handleValidatedNext( step ) {
      return (e) => {
        if ( getSetValidation( step ) ) {
          if ( that.state.hasError ) {
            that.setState( {
              hasError: false
            } );
          }
          that.handleNext( e );
        } else {
          that.setState( {
            hasError: true
          } );
        }
      }
    }

    return (
    <div className={ classes.root }>
      <Stepper activeStep={ activeStep } className={ classes.stepper }>
        { steps.map( (label, index) => {
            const props = {};
            const labelProps = {};
            props.completed = false;
            return (
            <Step key={ label } {...props}>
              <StepLabel {...labelProps}>
                { label }
              </StepLabel>
            </Step>
            );
          } ) }
      </Stepper>
      <div>
        { activeStep === steps.length ? (
            <div>
              <Typography className={ classes.instructions }>
                { t( 'common.allStepsCompleted' ) }
              </Typography>
              <Button onClick={ this.handleReset } className={ classes.button }>
                { t( 'common.reset' ) }
              </Button>
            </div>
            ) : (
            <div>
              <div>
                <Button disabled={ activeStep === 0 }
                        onClick={ this.handleBack }
                        className={ classes.button }>
                  { t( 'common.back' ) }
                </Button>
                <Button variant="contained"
                        color="primary"
                        disabled={ activeStep === steps.length - 1 && creating }
                        onClick={ handleValidatedNext( activeStep )}
                        className={ classes.button }>
                  { activeStep === steps.length - 1 ? t( 'eventCreate.createEvent' ) : t( 'common.next' ) }
                </Button>
                { activeStep === steps.length - 1 ? <Button variant="contained"
                        color="secondary"
                        disabled={ creating }
                        onClick={ () => { that.handleCreateEvent(true) } }
                        className={ classes.button }>
                    { t( 'eventCreate.createAndPublish' ) }
                </Button> : null }
              </div>
              {/* 예전에는 서버가 거절해도 화면에 아무것도 안 떠서 버튼이 안 먹는 것처럼 보였다 */}
              { (activeStep === steps.length - 1 && createError) ? <Alert severity="error" sx={ { mx: 1, mb: 1 } }>
                  { t( 'eventCreate.error', { status: createError.status || '-', detail: createError.detail ? ' ' + createError.detail : '' } ) }
                </Alert> : null }
              <Typography component="div" className={ classes.instructions }>
                <Paper elevation={ 1 } style={ { padding: '20px 0px' } }>
                  { getStepContent( activeStep ) }
                </Paper>
              </Typography>
              
            </div>
            ) }
      </div>
    </div>
    );
  }
}

export default EventCreate;
