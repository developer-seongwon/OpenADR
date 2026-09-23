import React from 'react';

import classNames from 'classnames';


import { lighten } from '@mui/material/styles';


import { withStyles } from 'tss-react/mui';


import { VenAvailableReportHeader, VenAvailableReportParamsHeader } from '../common/VenAvailableReportHeader'








import Typography from '@mui/material/Typography';

import Divider from '@mui/material/Divider';







import Tooltip from '@mui/material/Tooltip';







import Button from '@mui/material/Button';








import CloudDownloadIcon from '@mui/icons-material/CloudDownload';


import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';

import Checkbox from '@mui/material/Checkbox';

import Toolbar from '@mui/material/Toolbar';


import {minutesToICalDuration, iCalDurationInSeconds} from '../../utils/time'
import {isActionReport, isHistoryReport, isTelemetryReport} from '../../utils/venReport'



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

let VenAvailableReportDescriptionTableToolbar = props => {
  const { numSelected, availableReport, classes, handeCreateRequestClick } = props;
  return (
    <Toolbar
      className={classNames(classes.root, {
        [classes.highlight]: numSelected > 0,
      })}
    >
      <div className={classes.title}>
        {numSelected > 0 ? (
          <Typography color="primary" variant="subtitle1">
            {numSelected} selected
          </Typography>
        ) : (
          <Typography variant="h6" id="tableTitle">
            Select Report Descriptions
          </Typography>
        )}
      </div>
      <div className={classes.spacer} />
      <div className={classes.actions}>
        {numSelected > 0 ? (
          <Tooltip title="Request selected report description">
           
          <Button variant="outlined" color="primary" aria-label="Add" className={classes.margin} onClick={handeCreateRequestClick}>
            <CloudDownloadIcon className={classes.margin} />
             {(availableReport.reportName === "METADATA_TELEMETRY_USAGE") ? "Subscribe" : "Request"}
          </Button>

          </Tooltip>
        ) : null}
      </div>
    </Toolbar>
  );
};

VenAvailableReportDescriptionTableToolbar = withStyles(VenAvailableReportDescriptionTableToolbar, toolbarStyles);

class VenAvailableReportDescriptionTable extends React.Component {

  constructor( props ) {
    super( props );
    this.state = {}
  }

  
  

  render() {
    const {classes, availableReportDescription, availableReport} = this.props
    return (
      <div>
        <Paper className={classes.root}>
          <VenAvailableReportDescriptionTableToolbar handeCreateRequestClick={this.props.handeCreateRequestClick} 
          availableReport={availableReport} numSelected={this.props.selected.length} />
          <Table className={classes.table}>
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox">
                  <Checkbox
                     color="primary"
                    indeterminate={this.props.selected.length > 0 && this.props.selected.length < availableReportDescription.length}
                    checked={this.props.selected.length === availableReportDescription.length}
                    onChange={this.props.handleSelectAllClick}
                  />
                </TableCell>
                <TableCell align="right">rID</TableCell>
                <TableCell align="right">ReportType</TableCell>
                <TableCell align="right">ReadingType</TableCell>
                <TableCell align="right">OadrMinPeriod</TableCell>
                <TableCell align="right">OadrMaxPeriod</TableCell>
                <TableCell align="right">OadrOnchange</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {availableReportDescription.map(row => {
                const isSelected = this.props.isSelected(row.rid);
                return (
                  <TableRow key={row.id}
                    hover
                        onClick={event => this.props.handleClick(event, row.rid)}
                        role="checkbox"
                        aria-checked={isSelected}
                        tabIndex={-1}
                        selected={isSelected}>
                    <TableCell padding="checkbox">
                       <Checkbox checked={isSelected}  color="primary"/>
                    </TableCell>
                    <TableCell align="right">{row.rid}</TableCell>
                    <TableCell align="right">{row.reportType}</TableCell>
                    <TableCell align="right">{row.readingType}</TableCell>
                    <TableCell align="right">{row.oadrMinPeriod}</TableCell>
                    <TableCell align="right">{row.oadrMaxPeriod}</TableCell>
                    <TableCell align="right">{(row.oadrOnChange) ? "True" : "False"}</TableCell>
                </TableRow>
              )
              })}
            </TableBody>
          </Table>
        </Paper>
      </div>
    );
  }
  
}




export class VenDetailCreateReport extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {
      start:null,
      duration:null,
      granularity: null,
      reportBackDuration:null,
      selected: [],
      hasError:false
    }
  }

  // 예전 componentWillReceiveProps 를 옮겼다.
  // 예전에는 부모가 다시 그릴 때마다 start, duration 을 리포트 값으로 덮어써서
  // 사용자가 입력하던 값이 다른 상태 변화에 휩쓸려 되돌아갈 수 있었다.
  // 리포트(availableReport) 자체가 바뀐 경우에만 채우도록 좁혔다. 디버그용 console.log 두 줄은 뺐다
  componentDidUpdate(prevProps) {
    if(prevProps.availableReport === this.props.availableReport) {
      return;
    }
    var newState = {}
    if(this.props.availableReport.start != null) {
      newState.start = this.props.availableReport.start;
    }
    if(this.props.availableReport.duration != null) {
      newState.duration = iCalDurationInSeconds(this.props.availableReport.duration) / 60;
    }
    this.setState(newState);
  }


  onStartChange = start => { this.setState({start}) }
  onDurationChange = duration => this.setState({duration})
  onGranularityChange = granularity => {this.setState({granularity})}
  onReportBackDurationChange = reportBackDuration => this.setState({reportBackDuration})


  handleSelectAllClick = (event) => {
    if (event.target.checked) {
      this.setState(state => ({ selected: this.props.availableReportDescription.map(n => n.rid) }));
      return;
    }
    this.setState({ selected: [] });
  };

  isSelected = rid => this.state.selected.indexOf(rid) !== -1;

  handeCreateRequestClick = () => {
    var start = this.state.start;
    var end = null;
    if(start != null) {
      end = new Date();
      end.setTime(start + this.state.duration * 60 * 1000);
      end = end.getTime();
    }
    
    var granularity = null;
    var reportBackDuration = null;
    if(this.state.granularity) {
     granularity =  minutesToICalDuration(this.state.granularity);
    }
    if(this.state.reportBackDuration) {
     reportBackDuration =  minutesToICalDuration(this.state.reportBackDuration);
    }

    var rid = null;
    if(this.state.selected.length < this.props.availableReportDescription.length) {
      rid = this.state.selected;
    }

    if(isHistoryReport(this.props.availableReport) && start != null && end != null){

      this.props.createRequestedReport(this.props.venId, this.props.reportSpecifierId
          , start, end, rid);
    }
    else if(isTelemetryReport(this.props.availableReport) && granularity != null 
      && reportBackDuration != null){
      this.props.createRequestedReportSubscription(this.props.venId, this.props.reportSpecifierId
          , granularity, reportBackDuration, rid);
    }
    else if(isActionReport(this.props.availableReport)) {
      this.props.createRequestedReport(this.props.venId, this.props.reportSpecifierId
          , null, null, rid);
      
    }
    else {
      this.setState({hasError: true});
    }
    
  }

  handleClick = (event, rid) => {
    const { selected } = this.state;
    const selectedIndex = selected.indexOf(rid);
    let newSelected = [];

    if (selectedIndex === -1) {
      newSelected = newSelected.concat(selected, rid);
    } else if (selectedIndex === 0) {
      newSelected = newSelected.concat(selected.slice(1));
    } else if (selectedIndex === selected.length - 1) {
      newSelected = newSelected.concat(selected.slice(0, -1));
    } else if (selectedIndex > 0) {
      newSelected = newSelected.concat(
        selected.slice(0, selectedIndex),
        selected.slice(selectedIndex + 1),
      );
    }

    this.setState({ selected: newSelected });
  }
  
 
  render() {
    const {classes, availableReportDescription, availableReport
      , venId, reportSpecifierId} = this.props;


    return (
    <div className={ classes.root } >
      <VenAvailableReportHeader availableReport={availableReport} classes={classes}/>
      <Divider style={ { marginBottom: '30px', marginTop: '20px' } } /> 

      {(!isActionReport(this.props.availableReport)) ? (
        <div>
          <VenAvailableReportParamsHeader availableReport={availableReport} classes={classes}
            start={this.state.start}
            duration={this.state.duration}
            granularity={this.state.granularity}
            reportBackDuration={this.state.reportBackDuration}
            onStartChange={this.onStartChange}
            onDurationChange={this.onDurationChange}
            onGranularityChange={this.onGranularityChange}
            onReportBackDurationChange={this.onReportBackDurationChange}
            hasError={this.state.hasError}

            />
          <Divider style={ { marginBottom: '30px', marginTop: '20px' } } /> 
        </div>
        ) : null}
      
    
      <VenAvailableReportDescriptionTable availableReport={availableReport} 
          availableReportDescription={availableReportDescription} 
          handleSelectAllClick={this.handleSelectAllClick}
          handleClick={this.handleClick}
          isSelected={this.isSelected}
          handeCreateRequestClick={this.handeCreateRequestClick}
          selected={this.state.selected}
          venId={venId}
          reportSpecifierId={reportSpecifierId}
          classes={classes}/>
    </div>
    );
  }
}

export default VenDetailCreateReport;
