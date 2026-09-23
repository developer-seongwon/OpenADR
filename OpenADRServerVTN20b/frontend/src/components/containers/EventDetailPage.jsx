import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';

import * as vtnConfigurationActions from '../../actions/vtnConfigurationActions';
import * as eventActions from '../../actions/eventActions';
import * as venActions from '../../actions/venActions';


import { withStyles } from 'tss-react/mui';

import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';

import EventDetailDescriptor from '../EventDetail/EventDetailDescriptor'
import EventDetailActivePeriod from '../EventDetail/EventDetailActivePeriod'
import EventDetailSignal from '../EventDetail/EventDetailSignal'
import EventDetailTarget from '../EventDetail/EventDetailTarget'
import EventDetailVenResponse from '../EventDetail/EventDetailVenResponse'

import { history } from '../../store/configureStore';


import { green, red } from '@mui/material/colors';


function TabContainer( props ) {
  return (
  <Typography component="div" style={ { padding: 8 * 3 } }>
    { props.children }
  </Typography>
  );
}

const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  container: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  textField: {
    marginLeft: theme.spacing(1),
    marginRight: theme.spacing(1),
  },
  dense: {
    marginTop: 19,
  },
  menu: {
    width: 200,
  },
  formControl: {
    margin: theme.spacing(1),
    display:'flex'
  },
  card: {
    maxWidth: 350,
    minWidth: 350,
  },
  media: {
    height: 40,
    paddingTop: 10,
    paddingRight: 10
  },
  button: {
    margin: theme.spacing(1),
  },
  success: {
    backgroundColor: green[600],
  },
  error: {
    backgroundColor: red[600],
  },
  iconButton: {
    marginTop: 10
  },
});

export class EventDetailPage extends React.Component {

  constructor() {
    super()
    this.state = {
      value: 0,
      copySignals: [],
      copyTargets: [],
      editMode: false
    };
  }

  handleChange = (event, value) => {
    this.setState( {
      value
    } );
    switch(value) {
      case 0:
        history.push("/event/detail/"+this.props.match.params.id+"/descriptor")
        break;
      case 1:
        history.push("/event/detail/"+this.props.match.params.id+"/activeperiod")
        break;
      case 2:
        history.push("/event/detail/"+this.props.match.params.id+"/signal")
        break;
      case 3:
        history.push("/event/detail/"+this.props.match.params.id+"/target")
        break;
      case 4:
        history.push("/event/detail/"+this.props.match.params.id+"/venresponse")
        break;
      default:
        break;
    }

  };

  // 탭 순서. Tabs 에 그린 순서와 같아야 하고, App.js 의 :panel 라우트 목록과도 같아야 한다
  static PANELS = ['descriptor', 'activeperiod', 'signal', 'target', 'venresponse'];

  panelIndex = (panel) => {
  	var i = EventDetailPage.PANELS.indexOf(panel);
  	return i < 0 ? 0 : i;
  }

  componentDidMount() {
    this.props.vtnConfigurationActions.loadMarketContext();
    this.props.vtnConfigurationActions.loadGroup();
    this.props.eventActions.loadEventDetail(this.props.match.params.id);
    this.props.eventActions.loadEventVenResponse(this.props.match.params.id);

    if(this.props.event_detail.event.signals) {
      this.setState({copySignals: this.props.event_detail.event.signals});
    }
    if(this.props.event_detail.event.targets) {
      this.setState({copyTargets: this.props.event_detail.event.targets});
    }

    this.setState({ value: this.panelIndex(this.props.match.params.panel) });
    

  }

  componentDidUpdate(prevProps, prevState) {
    // 주소가 다른 패널로 바뀌면 탭도 따라가야 한다.
    //
    // 탭 위치를 componentDidMount 에서만 정하면, 패널만 바뀔 때는 컴포넌트가 다시
    // 마운트되지 않으므로 주소만 바뀌고 탭은 첫 번째에 머문다.
    // 이 클래스에는 componentDidUpdate 가 이미 있으니 여기에 같이 둔다.
    // 따로 하나 더 정의하면 뒤에 오는 정의에 덮여서 조용히 무효가 된다.
    if (prevProps.match.params.panel !== this.props.match.params.panel) {
      this.setState({ value: this.panelIndex(this.props.match.params.panel) });
    }
    if(this.props.event_detail.event.signals !== prevProps.event_detail.event.signals) {
      this.setState({copySignals: this.props.event_detail.event.signals});
    }
    if(this.props.event_detail.event.targets !== prevProps.event_detail.event.targets) {
      this.setState({copyTargets: this.props.event_detail.event.targets});
    }

    
  }

  updateCopySignals = (index, newSignal) => {
    var copySignals = this.state.copySignals;
    copySignals[index] = newSignal;
    this.setState({copySignals: copySignals, editMode: true});
  }

  removeCopySignals = (index) => {
    var copySignals = this.state.copySignals;
    copySignals.splice(index, 1)
    this.setState({copySignals: copySignals, editMode: true});
  }

  updateCopyTargets = (newTargets) => {
    this.setState({copyTargets: newTargets, editMode: true});
  }

   addCopySignals = (index) => {
    var copySignals = this.state.copySignals;
    copySignals.push({
        signalName: "",
        signalType: "",
        unitType: "",
        intervals: [],
        currentValue: "",
      });
    this.setState({copySignals: copySignals, editMode: true});
  }

  updateEvent = (published) => {
    var dto = {
      published:published,
      signals: this.state.copySignals,
      targets: this.state.copyTargets
    }
    this.props.eventActions.updateEvent(this.props.match.params.id, dto)
    this.setState({editMode: false});
  }

  onVenSuggestionsFetchRequested = (e) => {
    var filters = [];
    filters.push({type:"VEN", value:e.value});
    this.props.venActions.searchVen(filters, 0, 5);
  }

  onVenSuggestionsClearRequested = () => {
  }

  onVenSuggestionsSelect = (ven) => {
     var filters = this.state.filters;
    filters.push({type:"VEN", value:ven.username});
    this.setState({filters});
    this.refreshEvent();
  }

  render() {
    const {classes, event_detail} = this.props;
    const {value} = this.state;
    if(!event_detail.event.activePeriod ) return null;
    return (
     <div className={ classes.root }>
      <Tabs value={ this.state.value }
            onChange={ this.handleChange }
            indicatorColor="primary"
            textColor="primary"
            centered>
        <Tab label="Descriptor" />
        <Tab label="Active Period" />
        <Tab label="Signals" />
        <Tab label="Targets" />
        <Tab label="Ven Responses" />
      </Tabs>
      <Divider variant="middle" />
      { value === 0 && <TabContainer>
                <EventDetailDescriptor classes={classes} event={event_detail.event} 
                  activeEvent={this.props.eventActions.activeEvent}
                  cancelEvent={this.props.eventActions.cancelEvent}
                  publishEvent={this.props.eventActions.publishEvent}/>
                     
                       </TabContainer> }     
      { value === 1 && <TabContainer>
                <EventDetailActivePeriod classes={classes} event={event_detail.event}/>
                     
                       </TabContainer> }
      { value === 2 && <TabContainer>
                <EventDetailSignal classes={classes} event={event_detail.event} 
                  updateEvent={this.updateEvent}
                   
                  
                  editMode={this.state.editMode}
                  copySignals={this.state.copySignals}
                  updateCopySignals={this.updateCopySignals}
                 
                  removeCopySignals={this.removeCopySignals}
                  addCopySignals={this.addCopySignals}
                  publishEvent={this.props.eventActions.publishEvent}
                  />
                  
                       </TabContainer> }
      { value === 3 && <TabContainer>
                <EventDetailTarget classes={classes} event={event_detail.event}

                  updateEvent={this.updateEvent}
                  group={event_detail.group}
                  editMode={this.state.editMode}
                  copyTargets={this.state.copyTargets}
                  updateCopyTargets={this.updateCopyTargets}
                   ven={event_detail.ven}
                onVenSuggestionsFetchRequested={this.onVenSuggestionsFetchRequested}
                onVenSuggestionsClearRequested={this.onVenSuggestionsClearRequested}
                onVenSuggestionsSelect={this.props.onVenSuggestionsSelect}
                  />
                     
                       </TabContainer> }

      { value === 4 && <TabContainer>
                <EventDetailVenResponse classes={classes} event={event_detail.event} venResponse={event_detail.venResponse}
                refreshVenResponse={() => {this.props.eventActions.loadEventVenResponse(this.props.match.params.id)}}
               />
                     
                       </TabContainer> }
                


    </div>

    );
  }
}

EventDetailPage.propTypes = {
  eventActions: PropTypes.object.isRequired
};

function mapStateToProps( state ) {
  return {
    event_detail: state.event_detail
  };
}

function mapDispatchToProps( dispatch ) {
  return {
    eventActions: bindActionCreators( eventActions, dispatch ),
    vtnConfigurationActions: bindActionCreators( vtnConfigurationActions, dispatch ),
    venActions: bindActionCreators( venActions, dispatch ),

    
  };
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)( withStyles(EventDetailPage, styles) );
