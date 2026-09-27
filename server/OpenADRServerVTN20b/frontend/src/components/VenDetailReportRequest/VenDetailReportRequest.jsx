import React from 'react';










import Typography from '@mui/material/Typography';




import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';

import Toolbar from '@mui/material/Toolbar';

import {formatTimestamp} from '../../utils/time'




export class VenDetailReportRequest extends React.Component {
  constructor( props ) {
    super( props );
    this.state = {}
  }

  render() {
    const {classes, requestedReportSpecifier} = this.props;

    var getFormatedDatetime = (datetime) => {
    var format = formatTimestamp(datetime);
    return (
      <span>{format.date}<br/>{format.time} {format.tz}</span>
    );
  }

    return (
    <div className={ classes.root } >
       <Paper className={classes.root}>
      <Toolbar
      className={classes.root}
    >
      <div className={classes.title}>
         <Typography variant="h6" id="tableTitle">
             Report Requests
          </Typography>
      </div>
      <div className={classes.spacer} />
      <div className={classes.actions}>
      </div>
    </Toolbar>
      <Table className={classes.table}>
        <TableHead>
          <TableRow>
            <TableCell align="right">rID</TableCell>
            <TableCell align="right">Archived</TableCell>
            <TableCell align="right">Last Update<br/>Date/Time</TableCell>
            <TableCell align="right">Last Update<br/>Value</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {requestedReportSpecifier.map(row => (
            <TableRow key={row.id}>
              <TableCell align="right">{row.rid}</TableCell>
              <TableCell align="right">{(row.archived) ? "Archived": ""}</TableCell>
              <TableCell align="right">{(row.lastUpdateDatetime != null) ? getFormatedDatetime(row.lastUpdateDatetime) : null } </TableCell>
              <TableCell align="right">{(row.lastUpdateValue != null) ? row.lastUpdateValue : null } </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Paper>
  
    </div>
    );
  }
}

export default VenDetailReportRequest;
