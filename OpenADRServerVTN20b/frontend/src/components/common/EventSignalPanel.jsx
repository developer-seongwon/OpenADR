import React from 'react';

import { lighten } from '@mui/material/styles';




import { withStyles } from 'tss-react/mui';




import FormControl from '@mui/material/FormControl';
import FormLabel from '@mui/material/FormLabel';


import TextField from '@mui/material/TextField';



import Grid from '@mui/material/Grid';



import MenuItem from '@mui/material/MenuItem';

import Select from '@mui/material/Select';



import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';

import AddIcon from '@mui/icons-material/Add';

import Button from '@mui/material/Button';

import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import Toolbar from '@mui/material/Toolbar';






// name 은 백엔드 DemandResponseEventSignalTypeEnum 의 상수 이름이다(API 가 LEVEL, PRICE_MULTIPLIER 처럼 이름으로 주고받는다).
// 원래 OpenADR 라벨(level, priceMultiplier)을 보내서 이벤트 생성이 실패하고 상세 화면 선택값이 비었다.
// signalName 쪽(SIMPLE, ENERGY_PRICE)은 원래부터 이름을 썼다. 화면에는 label 이 보인다
const signalTypeMenuItems = [{
    name: "DELTA",
    label: "Delta",
    description: "Signal indicates the amount to change from what one would have used without the signal",
  }, {
    name: "LEVEL",
    label: "Level",
    description: "Signal indicates a program level" 
  }, {
    name: "MULTIPLIER",
    label: "Multiplier",
    description: "Signal indicates a multiplier applied to the current rate of delivery or usage from what one would have used without the signal" 
  } , {
    name: "PRICE",
    label: "Price",
    description: "Signal indicates the price" 
  } , {
    name: "PRICE_MULTIPLIER",
    label: "Price Multiplier",
    description: "Signal indicates the price multiplier. Extended price is the computed price value multiplied by the number of units" 
  } , {
    name: "PRICE_RELATIVE",
    label: "Price Relative",
    description: "Signal indicates the relative price" 
  } , {
    name: "SETPOINT",
    label: "Set Point",
    description: "Signal indicates a target amount of units" 
  } , {
    name: "X_LOAD_CONTROL_CAPACITY",
    label: "Load Control Capacity",
    description: "This is an instruction for the load controller to operate at a level that is some percentage of its maximum load consumption capacity. This can be mapped to specific load controllers to do things like duty cycling. Note that 1.0 refers to 100% consumption. In the case of simple ON/OFF type devices then 0 = OFF and 1 = ON" 
  } , {
    name: "X_LOAD_CONTROL_LEVEL_OFFSET",
    label: "Load Control Level Offset",
    description: "Discrete integer levels that are relative to normal operations where 0 is normal operations" 
  }  , {
    name: "X_LOAD_CONTROL_PERCENT_OFFSET",
    label: "Load Control Percent Offset",
    description: "Percentage change from normal load control operations" 
  } , {
    name: "X_LOAD_CONTROL_SETPOINT",
    label: "Load Control Set Point",
    description: "Load controller set points" 
  } 
]

const signalNameMenuItems = [{
    name: "BID_ENERGY",
    label: "Bid Energy",
    description: "This is the amount of energy from a resource that was bid into a program" 
  },{
    name: "BID_LOAD",
    label: "Bid Load",
    description: "This is the amount of load that was bid by a resource into a program" 
  }, {
    name: "BID_PRICE",
    label: "Bid Price",
    description: "This is the price that was bid by the resource"

  }, {
    name: "CHARGE_STATE",
    label: "Charge State",
    description: "State of energy storage resource"

  }, {
    name: "DEMAND_CHARGE",
    label: "Demand Charge",
    description: "This is the demand charge"

  }, {
    name: "ELECTRICITY_PRICE",
    label: "Electricity Price",
    description: "This is the cost of electricity"

  }, {
    name: "ENERGY_PRICE",
    label: "Energy Price",
    description: "This is the cost of energy"

  }, {
    name: "LOAD_CONTROL",
    label: "Load Control",
    description: "Set load output to relative values"

  }, {
    name: "LOAD_DISPATCH",
    label: "Load Dispatch",
    description: "This is used to dispatch load"

  }, {
    name: "SIMPLE",
    label: "Simple",
    description: "Simple levels (OpenADR 2.0a compliant) "

  }, {
    name: "SIMPLE_DEPRECATED",
    label: "Simple (deprecated)",
    description: "depreciated - for backwards compatibility with A profile"

  }
];



const unitMenuItems = {

  currencyPerKwh: [{name: "euro_per_kwh",
    label: "€ / kWh",
    description: "Euro per kWh"
  }, {
    name: "dollar_per_kwh",
    label: "$ / kWh",
    description: "Euro per kWh"
  }],
  energy: [{
    name: "kwh",
    label: "kWh",
    description: "kWh"
  }],
  power: [{
    name: "kw",
    label: "kW",
    description: "kW"
  }],
  none:[{
    name: "none",
    label: "None",
    description: "None"
  }]
}

const validSignalCombination = [{
  signalName: "SIMPLE",
  signalType: "LEVEL",
  unit: unitMenuItems.none
}, {
  signalName: "ELECTRICITY_PRICE",
  signalType: "PRICE",
  unit: unitMenuItems.currencyPerKwh
} , {
  signalName: "ELECTRICITY_PRICE",
  signalType: "PRICE_RELATIVE",
  unit: unitMenuItems.currencyPerKwh
} , {
  signalName: "ELECTRICITY_PRICE",
  signalType: "PRICE_MULTIPLIER",
  unit: unitMenuItems.none
}, {
  signalName: "ENERGY_PRICE",
  signalType: "PRICE_RELATIVE",
  unit: unitMenuItems.currencyPerKwh
}, {
  signalName: "ENERGY_PRICE",
  signalType: "PRICE_MULTIPLIER",
  unit: unitMenuItems.none
}, {
  signalName: "ENERGY_PRICE",
  signalType: "PRICE",
  unit: unitMenuItems.currencyPerKwh
}, {
  signalName: "DEMAND_CHARGE",
  signalType: "PRICE_RELATIVE",
  unit: unitMenuItems.currencyPerKwh
}, {
  signalName: "DEMAND_CHARGE",
  signalType: "PRICE_MULTIPLIER",
  unit: unitMenuItems.none
}, {
  signalName: "DEMAND_CHARGE",
  signalType: "PRICE",
  unit: unitMenuItems.currencyPerKwh
}, {
  signalName: "BID_PRICE",
  signalType: "PRICE",
  unit: unitMenuItems.currencyPerKwh
}, {
  signalName: "BID_LOAD",
  signalType: "SETPOINT",
  unit: unitMenuItems.power
}, {
  signalName: "BID_ENERGY",
  signalType: "SETPOINT",
  unit: unitMenuItems.energy
}, {
  signalName: "CHARGE_STATE",
  signalType: "SETPOINT",
  unit: unitMenuItems.energy
}, {
  signalName: "CHARGE_STATE",
  signalType: "DELTA",
  unit: unitMenuItems.energy
}, {
  signalName: "CHARGE_STATE",
  signalType: "MULTIPLIER",
  unit: unitMenuItems.none
}, {
  signalName: "LOAD_DISPATCH",
  signalType: "SETPOINT",
  unit: unitMenuItems.power
}, {
  signalName: "LOAD_DISPATCH",
  signalType: "DELTA",
  unit: unitMenuItems.power
}, {
  signalName: "LOAD_DISPATCH",
  signalType: "MULTIPLIER",
  unit: unitMenuItems.none
}, {
  signalName: "LOAD_DISPATCH",
  signalType: "LEVEL",
  unit: unitMenuItems.power
}, {
  signalName: "LOAD_CONTROL",
  signalType: "X_LOAD_CONTROL_CAPACITY",
  unit: unitMenuItems.none
}, {
  signalName: "LOAD_CONTROL",
  signalType: "X_LOAD_CONTROL_PERCENT_OFFSET",
  unit: unitMenuItems.none
}, {
  signalName: "LOAD_CONTROL",
  signalType: "X_LOAD_CONTROL_SETPOINT",
  unit: unitMenuItems.none
}, {
  signalName: "LOAD_CONTROL",
  signalType: "X_LOAD_CONTROL_LEVEL_OFFSET",
  unit: unitMenuItems.none
}];

var getAvailableSignalType = (signalName) => {
  var validSignalType = [];
  for(var i in validSignalCombination) { 
    var combination = validSignalCombination[i];
    if(combination.signalName === signalName){
      validSignalType.push(combination.signalType)
    }
  }
  return validSignalType;
}

var getAvailableUnitType = (signalName, signalType) => {
  var validUnitType = [];
  for(var i in validSignalCombination) { 
    var combination = validSignalCombination[i];
    if(combination.signalName === signalName && combination.signalType === signalType){
      validUnitType = validUnitType.concat(combination.unit);
      
    }
  }
  return validUnitType;
}

const labelStyle = {

  boxSizing: 'border-box',
  color: 'rgba(0, 0, 0, 0.54)',
  fontSize: '1rem',
  fontWeight: 400,

  lineHeight: 1,
  transition: 'color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms',
  transform: 'translate(0, 1.5px) scale(0.75)',
  transformOrigin: 'top left',
  top: 0,
  left: 0,
  marginTop: "-8px"
}

const toolbarStyles = theme => ({
  root: {
    paddingRight: theme.spacing(1),
  },
  highlight:
    theme.palette.mode === 'light'
      ? {
          color: theme.palette.secondary.main,
          backgroundColor: lighten(theme.palette.primary.light, 0.85),
        }
      : {
          color: theme.palette.text.primary,
          backgroundColor: theme.palette.secondary.dark,
        },
  spacer: {
    flex: '1 1 100%',
  },
  actions: {
    color: theme.palette.text.secondary,
  },
  title: {
    flex: '0 0 auto',
  },
  margin: {
    marginRight: '10px',
  },
});

var SignalIntervalTable = (props) => {
  const {classes} = props;
  return (
    <Paper >
      <Toolbar>
        <Grid container spacing={ 3 }>
          <Grid size={4}>
            <TextField label="Interval Duration (min)" required
                        type="number"
                       placeholder="Duration"
                       value={ props.createIntervalDuration }
                       className={ classes.textField }
                       onChange={ props.handleCreateIntervalDurationChange }
                       fullWidth={ true }
                       slotProps={{
                         inputLabel: { shrink: true }
                       }}/>
          </Grid> 
          <Grid size={4}>
            <TextField label="Interval Value" required
                       placeholder="Value"
                       value={ props.createIntervalValue }
                       className={ classes.textField }
                       onChange={ props.handleCreateIntervalValueChange }
                       fullWidth={ true }
                       slotProps={{
                         inputLabel: { shrink: true }
                       }} />
          </Grid> 
          {(props.needIntervalCreate && props.createIntervalValue !== "" && props.createIntervalDuration !== "") ? <Grid size={2}>
            <Button key="btn_create"
                            variant="outlined"
                            color="primary"
                            size="small"
                            style={ { marginTop: 13 } }
                            onClick={ props.handleCreateIntervalClick }>
                      <AddIcon />{(props.createIntervalEditMode ) ? "Edit" : "Create"}
                    </Button>
          </Grid>: null}

          {(props.needIntervalCreate && props.createIntervalValue !== "" && props.createIntervalDuration !== "") ? <Grid size={2}>
            <Button key="btn_create"
                            variant="outlined"
                            color="secondary"
                            size="small"
                            style={ { marginTop: 13 } }
                            onClick={ props.handleCancelIntervalClick }>
                      <AddIcon />Cancel
                    </Button>
          </Grid>: null}
          
          
      </Grid>
      </Toolbar>
      {(props.intervals.length > 0) ? <Table className={classes.table}>
        <TableHead>
          <TableRow>
            <TableCell align="right">Duration</TableCell>
            <TableCell align="right">Value</TableCell>
            <TableCell align="right"></TableCell>
            <TableCell align="right"></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {props.intervals.map( (row, index) => (
            <TableRow key={index}>
              <TableCell scope="row" align="right">{row.duration}</TableCell>
              <TableCell scope="row" align="right">{row.value}</TableCell>
              <TableCell scope="row" align="right">
                  <Button size="small" color="primary" onClick={props.handleEditIntervalClick(row, index)}> EDIT </Button>
              </TableCell>
              <TableCell scope="row" align="right">
                  <Button size="small" color="secondary" onClick={props.handleRemoveSignalIntervalAtIndex(index)}> REMOVE </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table> : null}

    </Paper>
  );
}

SignalIntervalTable = withStyles(SignalIntervalTable, toolbarStyles);


export class EventSignalPanel extends React.Component {

  constructor( props ) {
    super( props );
    this.state = {};
    this.state.createIntervalDuration = "";
    this.state.createIntervalValue = "";
    this.state.createIntervalEditMode = false;
    this.state.createIntervalEditIndex = null;
    this.state.needIntervalCreate = false;
   

    if(props.eventSignal.intervals.length > 0) {
      this.state.needIntervalCreate = true;
    }
  }

  // 예전 componentWillReceiveProps 를 옮겼다. 그건 부모가 다시 그릴 때마다 불렸고
  // 자기 setState 때는 안 불렸다. 그래서 props 객체가 바뀐 경우(부모가 다시 그린 경우)만 본다.
  // getDerivedStateFromProps 로 옮기면 사용자가 체크를 끄는 setState 에도 불려서
  // 구간이 있는 동안은 체크를 아예 끌 수 없게 된다. 동작을 바꾸지 않으려고 componentDidUpdate 로 했다
  componentDidUpdate(prevProps) {
    if(prevProps !== this.props
        && this.props.eventSignal.intervals.length > 0
        && !this.state.needIntervalCreate) {
      this.setState({needIntervalCreate: true});
    }
  }

  handleSignalNameChange = (e) => {
    let signal = this.props.eventSignal;
    signal.signalName = e.target.value;
    

    var availableSignalType = getAvailableSignalType(signal.signalName);
    if(availableSignalType.length === 1) {
      signal.signalType = availableSignalType[0];
      var availableUnitType = getAvailableUnitType(signal.signalName, signal.signalType);
      if(availableUnitType.length === 1) {
        signal.unitType = availableUnitType[0].name;
      }
    }

    this.props.onChange(signal);
  }

  handleSignalTypeChange = (e) => {
    let signal = this.props.eventSignal;
    signal.signalType = e.target.value;
    this.props.onChange(signal);
  }

  handleUnitTypeChange = (e) => {
    let signal = this.props.eventSignal;
    signal.unitType = e.target.value;
    this.props.onChange(signal);
  }

  handleRemoveSignalIntervalAtIndex = (index) => () => {
    let signal = this.props.eventSignal;
    signal.intervals.splice(index,1);
    this.props.onChange(signal);
  }

  handleCreateIntervalDurationChange = (e) => {
    this.setState({createIntervalDuration: e.target.value});
  }

  handleCreateIntervalValueChange = (e) => {
    this.setState({createIntervalValue: e.target.value});
  }

  handleCurrentValueChange = (e) => {
    let signal = this.props.eventSignal;
    signal.currentValue = e.target.value;
    this.props.onChange(signal);
  }
  
  handleCreateIntervalClick = () => {
    let signal = this.props.eventSignal;
    let interval = {
        duration: this.state.createIntervalDuration,
        value: this.state.createIntervalValue
      }
    if(this.state.createIntervalEditMode){
      signal.intervals[this.state.createIntervalEditIndex] = interval;
    }
    else {
      signal.intervals.push(interval);
    }
    
    this.props.onChange(signal);
    this.setState({
      createIntervalEditMode: false,
      createIntervalEditIndex: null,
      createIntervalDuration: "",
      createIntervalValue: ""
    })
  }

  handleEditIntervalClick =(interval, index) => () => {
    this.setState({
      createIntervalEditMode: true,
      createIntervalDuration: interval.duration,
      createIntervalValue: interval.value,
      createIntervalEditIndex: index
    })
  }

  handleCancelIntervalClick = () => {
    this.setState({
      createIntervalEditMode: false,
      createIntervalEditIndex: null,
      createIntervalDuration: "",
      createIntervalValue: ""
    })
  }

  handleNeedIntervalCreateClick = (e) => {
    if(!e.target.checked) {
      let signal = this.props.eventSignal;
      signal.intervals = [];
      this.props.onChange(signal);
    }
    this.setState({needIntervalCreate: e.target.checked});
  }


  render() {
    const {classes, hasError, eventSignal} = this.props;

    var signalNameView = [];
    var s;
    for(var i in signalNameMenuItems){
      s  = signalNameMenuItems[i];
      signalNameView.push(<MenuItem key={"menu_item_signal_name_"+s.name} value={s.name}>
                       {s.label}
                       </MenuItem> )
    }

    var signalTypeView = [];
    if(eventSignal.signalName !== "") {
      var validSignalType = getAvailableSignalType(eventSignal.signalName);
      for(var j in signalTypeMenuItems){
        s  = signalTypeMenuItems[j];
        if(validSignalType.includes(s.name) ){
          signalTypeView.push(<MenuItem key={"menu_item_signal_name_"+s.name} value={s.name}>
                         {s.label}
                         </MenuItem> );
        }
        
      }
    }

    var unitTypeView = [];
     if(eventSignal.signalType !== "") {
      var validUnitType = getAvailableUnitType(eventSignal.signalName, eventSignal.signalType);
      for(var k in validUnitType){
        s = validUnitType[k];
        unitTypeView.push(<MenuItem key={"menu_item_unit_type_"+s.name} value={s.name}>
             {s.label}
             </MenuItem> );
        
      }
    }

    
    



    return (
      <Grid container
            spacing={ 1 }
            sx={{
              justifyContent: "center"
            }}>

        <Grid container spacing={ 3 }>
          <Grid size={4}>
            <FormControl className={ classes.formControl }>
                  <FormLabel style={ labelStyle } component="label">
                    Signal Name
                  </FormLabel>
                  <Select required value={ eventSignal.signalName} error={ hasError && eventSignal.signalName === ""}
                                style={ { marginTop: 0 } }

                                 onChange={this.handleSignalNameChange}
                     
                                inputProps={ { name: 'signal_name_select', id: 'signal_name_select', } }
                                >
                        {signalNameView}
                  </Select>
                </FormControl>

          </Grid> 
          <Grid size={4}>
            <FormControl className={ classes.formControl }>
                  <FormLabel style={ labelStyle } component="label">
                    Signal Name
                  </FormLabel>
                  <Select required disabled={eventSignal.signalName === ""} 
                          error={ hasError && eventSignal.signalType === ""}
                          value={ eventSignal.signalType}
                                style={ { marginTop: 0 } }

                                 onChange={this.handleSignalTypeChange}
                     
                                inputProps={ { name: 'signal_type_select', id: 'signal_type_select', } }
                                >
                        {signalTypeView}
                  </Select>
                </FormControl>

          </Grid> 
          <Grid size={4}>
            <FormControl className={ classes.formControl }>
                  <FormLabel style={ labelStyle } component="label">
                    Signal Unit
                  </FormLabel>
                  <Select required disabled={eventSignal.signalName === "" || eventSignal.signalType === ""} value={ eventSignal.unitType}
                                style={ { marginTop: 0 } }
                              error={ hasError && eventSignal.unitType === ""}
                                 onChange={this.handleUnitTypeChange}
                     
                                inputProps={ { name: 'unit_type_select', id: 'unit_type_select', } }
                                >
                        {unitTypeView}
                  </Select>
                </FormControl>
          </Grid>
        </Grid>
        <Grid container spacing={ 3 }>
          <Grid size={4}>
              <TextField label="Current Value"
                         placeholder="Current Value"
                         error={ hasError && eventSignal.currentValue === "" && eventSignal.intervals.length === 0}
                         value={ eventSignal.currentValue }
                         className={ classes.textField }
                         onChange={ this.handleCurrentValueChange }
                         fullWidth={ true } />
            </Grid> 
          <Grid size={4}>
             <FormControlLabel
            control={
              <Checkbox
                checked={this.state.needIntervalCreate}
                onChange={this.handleNeedIntervalCreateClick}
                value="Intervals"
                color="primary"
              />
            }
            label="Intervals"
          />
          </Grid>
          {(this.props.canBeRemoved) ? <Grid size={4}>
             <Button key="btn_create"
                              variant="outlined"
                              color="secondary"
                              size="small"
                              className={ classes.button }
                              onClick={ this.props.onRemove }>
                        <AddIcon />Remove
                      </Button>
          </Grid>: null}
          
          
        </Grid>

        {(this.state.needIntervalCreate) ? <Grid container spacing={ 3 }
           style={ { marginTop: 20 , marginBottom:10 } }>
          <Grid size={12}>
            <SignalIntervalTable intervals={eventSignal.intervals} 
            handleRemoveSignalIntervalAtIndex={this.handleRemoveSignalIntervalAtIndex}
            needIntervalCreate={this.state.needIntervalCreate}
            createIntervalValue={this.state.createIntervalValue}
            createIntervalDuration={this.state.createIntervalDuration}
            createIntervalEditMode={this.state.createIntervalEditMode}
            handleCreateIntervalDurationChange={this.handleCreateIntervalDurationChange}
            handleCreateIntervalValueChange={this.handleCreateIntervalValueChange}
            handleCreateIntervalClick={this.handleCreateIntervalClick}
            handleEditIntervalClick={this.handleEditIntervalClick}
            handleCancelIntervalClick={this.handleCancelIntervalClick}
            />
          </Grid> 
        </Grid>: null}

      </Grid>
    );
  }
}

export default EventSignalPanel;
