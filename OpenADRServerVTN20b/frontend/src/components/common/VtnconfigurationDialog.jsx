import React from 'react';


import Avatar from '@mui/material/Avatar';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import ListItemText from '@mui/material/ListItemText';
import DialogTitle from '@mui/material/DialogTitle';
import Dialog from '@mui/material/Dialog';

import ExtensionIcon from '@mui/icons-material/Extension';
import GroupWorkIcon from '@mui/icons-material/GroupWork';
import SettingsInputComponentIcon from '@mui/icons-material/SettingsInputComponent';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';

import Button from '@mui/material/Button';
import DialogActions from '@mui/material/DialogActions';

import {VenAutocomplete} from './Autocomplete'

import { amber } from '@mui/material/colors';

import ListItemButton from "@mui/material/ListItemButton";

export function MarketContextSelectDialog( props ) {
  return (
    <Dialog open={ props.open } onClose={ () => {
                                          props.close()
                                        } }>
      <DialogTitle>
        { props.title }
      </DialogTitle>
      <div>
        <List>
          { props.marketContext.map( context => (
              <ListItemButton
                onClick={ () => {
                            props.close( context )
                          } }
                key={ context.name }>
                <ListItemAvatar>
                  <Avatar style={ { backgroundColor: context.color } }>
                    <ExtensionIcon />
                  </Avatar>
                </ListItemAvatar>
                <ListItemText primary={ context.name } />
              </ListItemButton>
            ) ) }
        </List>
      </div>
    </Dialog>
  );
}

export function GroupSelectDialog( props ) {
  return (
    <Dialog open={ props.open } onClose={ () => {
                                          props.close()
                                        } }>
      <DialogTitle>
        { props.title }
      </DialogTitle>
      <div>
        <List>
          { props.group.map( g => (
              <ListItemButton
                onClick={ () => {
                            props.close( g )
                          } }
                key={ g.name }>
                <ListItemAvatar>
                  <Avatar style={ { backgroundColor: '#bbb' } }>
                    <GroupWorkIcon />
                  </Avatar>
                </ListItemAvatar>
                <ListItemText primary={ g.name } />
              </ListItemButton>
            ) ) }
        </List>
      </div>
    </Dialog>
  );
}

export function TargetSelectDialog( props ) {
  return (
    <Dialog open={ props.open } onClose={ () => {
                                          props.close()
                                        } }>
      <DialogTitle>
        { props.title }
      </DialogTitle>
      <div>
        <List>
          <ListItemButton
            onClick={ () => {
                        props.close( "group" )
                      } }>
            <ListItemAvatar>
              <Avatar style={ { backgroundColor: '#bbb' } }>
                <GroupWorkIcon /> 
              </Avatar>
            </ListItemAvatar>
            <ListItemText primary="Group" />
          </ListItemButton>
          <ListItemButton
            onClick={ () => {
                        props.close( "ven" )
                      } }>
            <ListItemAvatar>
              <Avatar style={ { backgroundColor: '#bbb' } }>
                <SettingsInputComponentIcon /> 
              </Avatar>
            </ListItemAvatar>
            <ListItemText primary="Ven" />
          </ListItemButton>
        </List>
      </div>
    </Dialog>
  );
}

export function VenStatusSelectDialog( props ) {
  var items = [{
    name: "online",
    color: "green"
  },{
    name: "offline",
    color: "#bbb"
  }]
  return (
    <Dialog open={ props.open } onClose={ () => {
                                          props.close()
                                        } }>
      <DialogTitle>
        { props.title }
      </DialogTitle>
      <div>
        <List>
          { items.map( g => (
              <ListItemButton
                onClick={ () => {
                            props.close( g )
                          } }
                key={ g.name }>
                <ListItemAvatar>
                  <Avatar style={ { backgroundColor: g.color } }>
                    <SettingsInputComponentIcon />
                  </Avatar>
                </ListItemAvatar>
                <ListItemText primary={ g.name } />
              </ListItemButton>
            ) ) }
          
        </List>
      </div>
    </Dialog>
  );
}

export function VenSelectDialog( props ) {
  return (
  <Dialog open={ props.open } onClose={ () => {
                                        props.close()
                                      } }>
    <DialogTitle>
      { props.title }
    </DialogTitle>
    <div>
      <List>
       <ListItem style={{marginBottom:250, width: 400}}>

        <VenAutocomplete  suggestions={props.suggestions}
        onSuggestionsFetchRequested={props.onSuggestionsFetchRequested}
        onSuggestionsClearRequested={props.onSuggestionsClearRequested}
        onSuggestionsSelect={props.onSuggestionsSelect}/>
      </ListItem>
        
      </List>
    </div>
  </Dialog>
  );
}

export function EventStatusSelectDialog( props ) {
  var items = [{
    name: "ACTIVE",
    color: "green",
    label: "Active"
  },{
    name: "CANCELLED",
    color: "red",
    label: "Cancelled"
  },{
    name: "PUBLISHED",
    color: "#bbb",
    label: "Published"
  },{
    name: "NOT_PUBLISHED",
    color: amber[700],
    label: "Not Published"
  },{
    name: "SENDABLE",
    color: "#bbb",
    label: "Sendable"
  },{
    name: "NOT_SENDABLE",
    color: "#bbb",
    label: "Not Sendable"
  }]
  return (
    <Dialog open={ props.open } onClose={ () => {
                                          props.close()
                                        } }>
      <DialogTitle>
        { props.title }
      </DialogTitle>
      <div>
        <List>
          { items.map( g => (
              <ListItemButton
                onClick={ () => {
                            props.close( g )
                          } }
                key={ g.name }>
                <ListItemAvatar>
                  <Avatar style={ { backgroundColor: g.color } }>
                    <CalendarTodayIcon />
                  </Avatar>
                </ListItemAvatar>
                <ListItemText primary={ g.label } />
              </ListItemButton>
            ) ) }
          
        </List>
      </div>
    </Dialog>
  );
}

export function EventCalendarDialog( props ) {
  if(!props.event) return null;
  return (
  <Dialog open={ props.open } onClose={ () => {
                                        props.close()
                                      } }>
    <DialogTitle>
      { props.event.title }
    </DialogTitle>
    <div>
      <List>
        <ListItem >
          <ListItemAvatar>
            <Avatar>
              <SettingsInputComponentIcon />
            </Avatar>
          </ListItemAvatar>
          <ListItemText primary={ props.event.marketContext } />
        </ListItem>
      </List>
    </div>
      <DialogActions>
        <Button onClick={props.close} color="primary">
          close
        </Button>
        <Button onClick={props.handleEventDetailClick} color="primary" autoFocus>
          Event Detail
        </Button>
      </DialogActions>
  </Dialog>
  );
}
