import React from 'react';
import { t } from '../../i18n';










import Typography from '@mui/material/Typography';




import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';

import Toolbar from '@mui/material/Toolbar';
import Button from '@mui/material/Button';
import RefreshIcon from '@mui/icons-material/Refresh';

import {formatTimestamp} from '../../utils/time'




export class VenDetailReportRequest extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {}
  }

  render() {
    const {classes, requestedReportSpecifier, requestedReport, reportData} = this.props;
    // 화면 주소의 요청 하나만 읽어 오므로(reportRequestId) 첫 번째가 이 요청이다
    var request = (requestedReport && requestedReport.length > 0) ? requestedReport[0] : null;
    var data = reportData || [];

    var getFormatedDatetime = (datetime) => {
      var format = formatTimestamp(datetime);
      return (
        <span>{format.date}<br/>{format.time} {format.tz}</span>
      );
    }

    return (
    <div className={ classes.root } >
      <Paper className={classes.root}>
        <Toolbar className={classes.root}>
          <div className={classes.title}>
            <Typography variant="h6" id="tableTitle">
              { t( 'report.requests' ) }
            </Typography>
            { request ? <Typography variant="body2" color="text.secondary">
                { t( 'report.requestSummary', { reportRequestId: request.reportRequestId,
                  granularity: request.granularity, reportBackDuration: request.reportBackDuration } ) }
                { ' ' }{ request.acked ? t( 'report.acked' ) : t( 'report.notAcked' ) }
              </Typography> : null }
          </div>
          <div className={classes.spacer} />
          <div className={classes.actions}>
            { this.props.onRefresh ? <Button size="small" color="primary" onClick={ this.props.onRefresh }>
                <RefreshIcon style={ { marginRight: 8 } } />{ t( 'report.refresh' ) }
              </Button> : null }
          </div>
        </Toolbar>
        <Table className={classes.table}>
          <TableHead>
            <TableRow>
              <TableCell align="right">rID</TableCell>
              <TableCell align="right">{ t( 'report.col.archived' ) }</TableCell>
              <TableCell align="right">{ t( 'report.col.lastUpdate' ) }<br/>{ t( 'report.col.dateTime' ) }</TableCell>
              <TableCell align="right">{ t( 'report.col.lastUpdate' ) }<br/>{ t( 'report.col.value' ) }</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {requestedReportSpecifier.map(row => (
              <TableRow key={row.id}>
                <TableCell align="right">{row.rid}</TableCell>
                <TableCell align="right">{(row.archived) ? t( 'report.archived' ) : ""}</TableCell>
                <TableCell align="right">{(row.lastUpdateDatetime != null) ? getFormatedDatetime(row.lastUpdateDatetime) : null } </TableCell>
                <TableCell align="right">{(row.lastUpdateValue != null) ? row.lastUpdateValue : null } </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>

      {/* 받은 값. 서버는 보관(archived)을 켠 rID 의 값만 남긴다(Oadr20bVTNEiReportService.oadrUpdateReport) */}
      <Paper className={classes.root} style={ { marginTop: 24 } }>
        <Toolbar className={classes.root}>
          <div className={classes.title}>
            <Typography variant="h6">
              { t( 'report.receivedData' ) }
            </Typography>
            <Typography variant="body2" color="text.secondary">
              { t( 'report.receivedDataHelp' ) }
            </Typography>
          </div>
        </Toolbar>
        <Table className={classes.table}>
          <TableHead>
            <TableRow>
              <TableCell align="right">{ t( 'report.col.start' ) }</TableCell>
              <TableCell align="right">rID</TableCell>
              <TableCell align="right">{ t( 'report.col.value' ) }</TableCell>
              <TableCell align="right">{ t( 'report.duration' ) }</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            { (data.length === 0) ? <TableRow>
                <TableCell colSpan={ 4 } align="center">{ t( 'report.noData' ) }</TableCell>
              </TableRow> : null }
            { data.map(row => (
              <TableRow key={ row.id }>
                <TableCell align="right">{ (row.start != null) ? getFormatedDatetime(row.start) : null }</TableCell>
                <TableCell align="right">{ row.rid }</TableCell>
                <TableCell align="right">{ row.value }</TableCell>
                <TableCell align="right">{ row.duration }</TableCell>
              </TableRow>
            )) }
          </TableBody>
        </Table>
      </Paper>
    </div>
    );
  }
}

export default VenDetailReportRequest;
