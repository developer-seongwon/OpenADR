import React from 'react';



import FormControl from '@mui/material/FormControl';
import FormLabel from '@mui/material/FormLabel';

import FormControlLabel from '@mui/material/FormControlLabel';


import TextField from '@mui/material/TextField';



import Grid from '@mui/material/Grid';



import MenuItem from '@mui/material/MenuItem';

import Select from '@mui/material/Select';

import Divider from '@mui/material/Divider';




import Checkbox from '@mui/material/Checkbox';




import { MarketContextSelectDialog } from '../common/VtnconfigurationDialog'

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

export class EventCreateDescriptorStep extends React.Component {

  constructor( props ) {
    super( props );
    this.state = {
      marketContextSelectDialogOpen: false
    }
  
  }

 

  handleMarketContextSelectOpen = () => {
    this.setState({marketContextSelectDialogOpen: true})
  }

  handleMarketContextSelectClose = (context) => {
    if(context != null) {
      let details = this.props.descriptor;
      details.marketContext = context.name;
      this.props.onChange(details);
    }
    this.setState({marketContextSelectDialogOpen: false})
  }

  handlePriorityChange = (e) => {
    let details = this.props.descriptor;
    details.priority = e.target.value;
    this.props.onChange(details);
  }

  handleResponseRequiredChange = (e) => {
    let details = this.props.descriptor;
    details.responseRequired = e.target.value;
    this.props.onChange(details);
  }

  handleTestEventChange = (e) => {
    let details = this.props.descriptor;
    details.testEvent = e.target.checked;
    this.props.onChange(details);
  }

  handleVtnCommentChange = (e) => {
    let details = this.props.descriptor;
    details.vtnComment = e.target.value;
    this.props.onChange(details);
  }

  handleOadrProfileChange = (e) => {
     let details = this.props.descriptor;
    details.oadrProfile = e.target.value;
    this.props.onChange(details);
  }

  render() {
    const {classes, hasError, descriptor, marketContext} = this.props;


    return (
      <Grid container
            spacing={ 1 }
            sx={{
              justifyContent: "center"
            }}>

        <Grid container spacing={ 3 } >
        <Grid size={2} />
        <Grid size={2}>
           <FormControl className={ classes.formControl }>
                <FormLabel style={ labelStyle } component="label">
                  OadrProfile
                </FormLabel>
                <Select value={ descriptor.oadrProfile}
                              style={ { marginTop: 0 } }

                               onChange={this.handleOadrProfileChange}
                   
                              inputProps={ { name: 'response_required_select', id: 'response_required_select', } }
                              >
                      <MenuItem key="menu_item_oadr_profile_20a" value="OADR20A">
                       2.0a
                       </MenuItem> 

                       <MenuItem key="menu_item_oadr_profile_20a" value="OADR20B">
                       2.0b
                       </MenuItem> 
                </Select>
              </FormControl>
        </Grid>
        <Grid size={2}>
     
              <TextField required label="Priority"
                type="number"
                value={ descriptor.priority }
                onChange={this.handlePriorityChange}
                className={classes.textField}
                fullWidth={true}/>
  
                                         
        </Grid> 
        
        <Grid size={2}>
             <FormControl className={ classes.formControl }>
                <FormLabel style={ labelStyle } component="label">
                  Response Required
                </FormLabel>
                <Select value={ descriptor.responseRequired}
                              style={ { marginTop: 0 } }

                               onChange={this.handleResponseRequiredChange}
                   
                              inputProps={ { name: 'response_required_select', id: 'response_required_select', } }
                              >
                      <MenuItem key="response_required_item_always" value="ALWAYS">
                       Always
                       </MenuItem> 

                       <MenuItem key="response_required_item_never" value="NEVER">
                       Never
                       </MenuItem> 
                </Select>
              </FormControl>
        </Grid>
         <Grid size={2}>
           <FormControlLabel
          control={
            <Checkbox
              checked={descriptor.testEvent}
              onChange={this.handleTestEventChange}
              value="testEvent"
              color="primary"
            />
          }
          label="Test Event"
        />
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

        <Grid container spacing={ 3 } style={ { marginTop: 20, marginBottom:10  } }>
         <Grid size={2} />
        
        
        <Grid size={4}>
      
               <TextField required label="Market Context" error={hasError && descriptor.marketContext == null}
                  value={ (descriptor.marketContext) ? descriptor.marketContext : "" }
                  className={classes.textField}
                  fullWidth={true}
                  onClick={this.handleMarketContextSelectOpen}
                  slotProps={{
                    input: { readOnly: true, }
                  }} />

               <MarketContextSelectDialog marketContext={ marketContext}
                                          open={ this.state.marketContextSelectDialogOpen }
                                          close={ this.handleMarketContextSelectClose }
                                          title="Select Market Context" />
   
                                          
         </Grid> 
         
          <Grid size={4}>
             <TextField  label="VTN Comment"
                 multiline
             maxRows="4"
                   value={ descriptor.vtnComment }
                   onChange={this.handleVtnCommentChange}
                   className={classes.textField}
                   fullWidth={true}/>
           </Grid>
           
          
           <Grid size={2} />
         </Grid>

      </Grid>
    );
  }
}

export default EventCreateDescriptorStep;
