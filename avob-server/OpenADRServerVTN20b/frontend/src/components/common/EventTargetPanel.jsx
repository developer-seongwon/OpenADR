import React from 'react';
import { t } from '../../i18n';







import TextField from '@mui/material/TextField';

import Grid from '@mui/material/Grid';








import AddIcon from '@mui/icons-material/Add';

import Button from '@mui/material/Button';

import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';





import { GroupSelectDialog, TargetSelectDialog } from './VtnconfigurationDialog'

import {VenAutocomplete} from './Autocomplete'




// 서버는 대상 유형을 VEN, GROUP 으로 주고받는다. 표에는 번역한 이름을 보여 준다(확인 단계도 쓴다)
export var targetTypeLabel = (targetType) => {
  var type = String(targetType || '').toUpperCase();
  if (type === 'VEN') {
    return t( 'target.ven' );
  }
  if (type === 'GROUP') {
    return t( 'target.group' );
  }
  return targetType;
}

var EventTargetTable = (props) => {
  const {classes} = props;
  return (
    <Paper className={classes.root}>
      <Table className={classes.table}>
        <TableHead>
          <TableRow>
            <TableCell align="right">{ t( 'event.targetType' ) }</TableCell>
            <TableCell align="right">{ t( 'event.targetId' ) }</TableCell>
            <TableCell align="right"></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {props.eventTarget.map( (row, index) => (
            <TableRow key={index}>
              <TableCell scope="row" align="right">{targetTypeLabel(row.targetType)}</TableCell>
              <TableCell scope="row" align="right">{row.targetId}</TableCell>
              <TableCell scope="row" align="right">
                  <Button size="small" color="secondary" onClick={props.handleRemoveTargetAtIndex(index)}>{ t( 'common.remove' ) }</Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Paper>
  );
}

export class EventTargetPanel extends React.Component {

  constructor( props ) {
    super( props )
    this.state = {
      createTargetType: "",
      createTargetId: "",
      targetSelectDialog: false,
      groupSelectDialog: false
    }
  }

  // 받은 배열을 고치지 않고 새 배열을 넘긴다. 이벤트 상세는 리덕스에서 읽은 배열을 그대로 받던 적이 있어
  // 제자리에서 push, splice 하면 저장도 안 했는데 스토어 값이 바뀌었다
  handleCreateTarget = () => {
    // 고르는 창은 ven, group 을 돌려주지만 서버 TargetTypeEnum 은 VEN, GROUP 만 읽는다(소문자면 400)
    var created = {
      targetType: this.state.createTargetType.toUpperCase(),
      targetId: this.state.createTargetId
    };
    var exists = this.props.eventTarget.some((target) =>
      String(target.targetType).toUpperCase() === created.targetType && target.targetId === created.targetId);
    if (!exists) {
      this.props.onChange(this.props.eventTarget.concat([ created ]));
    }
    this.setState({
      createTargetType: "",
      createTargetId: "",
    });
  }

  handleCancelTarget = () => {
    this.setState({
      createTargetType: "",
      createTargetId: "",
    });
  }

  handleRemoveTargetAtIndex = (index) => () => {
    this.props.onChange(this.props.eventTarget.filter((target, i) => i !== index));
  }

  handleTargetTypeChange = (e) => {
    this.setState({createTargetType: e.target.value});
  }

  handleTargetIdChange = (e) => {
    this.setState({createTargetId: e.target.value});
  }

  handleTargetSelectDialogClose = (targetType) => {
    var newState = {targetSelectDialog: false}
    if(targetType != null){
      newState.createTargetType = targetType;
      // 유형을 바꾸면 앞서 고른 그룹 이름이나 VenID 는 버린다
      if (targetType !== this.state.createTargetType) {
        newState.createTargetId = "";
      }
    }
    this.setState(newState);
  }

  handleTargetSelectDialogOpen = () => {
    this.setState({targetSelectDialog: true});
  }

  handleGroupSelectClose = (group) => {
    var newState = {groupSelectDialog: false}
    if(group != null){
      newState.createTargetId = group.name;
    }
    this.setState(newState);
  }

  handleGroupSelectOpen = () => {
    this.setState({groupSelectDialog: true});
  }

  // 입력칸을 지우면 null 이 온다
  onVenSuggestionsSelect = (ven) => {
    this.setState({createTargetId: (ven != null) ? ven.username : ""});
  }


  render() {
    const {classes, hasError, eventTarget, group, marketContext} = this.props;
    // 대상 단계에서 다음을 눌렀는데 목록이 비었을 때
    var targetMissing = Boolean(hasError) && eventTarget.length === 0;

    return (
      <Grid container
            spacing={ 1 }
            sx={{
              justifyContent: "center"
            }}>

        <Grid container spacing={ 3 }>
            <Grid size={2}>
             <TextField
               label={ t( 'event.selectTargetType' ) }
               error={targetMissing}
               value={ this.state.createTargetType ? targetTypeLabel(this.state.createTargetType) : "" }
               placeholder={ t( 'event.targetTypePlaceholder' ) }
               className={classes.textField}
               fullWidth={true}
               onClick={this.handleTargetSelectDialogOpen}
               slotProps={{
                 input: { readOnly: true, },
                 inputLabel: { shrink: true }
               }} />

                <TargetSelectDialog open={ this.state.targetSelectDialog }
                                    close={ this.handleTargetSelectDialogClose }
                                    title={ t( 'event.selectTargetType' ) } />


              
            </Grid>

            { (this.state.createTargetType === "group") ? <Grid size={6}>
              <TextField required label={ t( 'event.group' ) } 
                   value={this.state.createTargetId}
                   className={classes.textField}
                   fullWidth={true}
                   onClick={this.handleGroupSelectOpen}
                   slotProps={{
                     input: { readOnly: true }
                   }} />

                <GroupSelectDialog group={ group}
                                           open={ this.state.groupSelectDialog }
                                           close={ this.handleGroupSelectClose }
                                           title={ t( 'event.selectGroup' ) } /* 그룹 고르는 창인데 제목이 Select Market Context 였다 */ />
            </Grid> : null}

            { (this.state.createTargetType === "ven") ? <Grid size={6}>
              <VenAutocomplete  suggestions={this.props.ven}
                onSuggestionsFetchRequested={this.props.onVenSuggestionsFetchRequested}
                onSuggestionsClearRequested={this.props.onVenSuggestionsClearRequested}
                onSuggestionsSelect={this.onVenSuggestionsSelect}/>
              { marketContext ? <Typography variant="caption" color="text.secondary" component="div" sx={ { mt: 0.5 } }>
                  { t( 'event.venSearchHint', { marketContext: marketContext } ) }
                </Typography> : null }
            </Grid> : null}

            { (this.state.createTargetType !== "" && this.state.createTargetId !== "") ? <Grid size={2}>
              <Button key="btn_create"
                              variant="outlined"
                              color="primary"
                              size="small"
                              className={ classes.button }
                              style={ { marginTop: 13 } }
                              onClick={ this.handleCreateTarget }>
                        <AddIcon />{ t( 'common.create' ) }
                      </Button>
            </Grid> : null}

            { (this.state.createTargetType !== "") ? <Grid size={2}>
              <Button key="btn_cancel"
                              variant="outlined"
                              color="secondary"
                              size="small"
                              className={ classes.button }
                              style={ { marginTop: 13 } }
                              onClick={ this.handleCancelTarget }>
                        <AddIcon />{ t( 'common.cancel' ) }
                      </Button>
            </Grid> : null}
        </Grid>
        { (targetMissing || (this.state.createTargetType !== "" && this.state.createTargetId !== "")) ?
          <Grid container spacing={ 3 } style={ { marginTop: 8 } }>
            <Grid size={12}>
              <Typography variant="body2" sx={ { color: targetMissing ? 'error.main' : 'text.secondary' } }>
                { t( 'event.targetAddHint' ) }
              </Typography>
            </Grid>
          </Grid> : null }
        <Grid container spacing={ 3 }
           style={ { marginTop: 20, marginBottom:10 } }>
          <Grid size={12}>
            <EventTargetTable classes={classes} eventTarget={eventTarget} 
            handleRemoveTargetAtIndex={this.handleRemoveTargetAtIndex}/>
          </Grid> 
        </Grid>

      </Grid>
    );
  }
}

export default EventTargetPanel;
