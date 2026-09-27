import React from 'react';

import Avatar from '@mui/material/Avatar';

import Card from '@mui/material/Card';
import CardActionArea from '@mui/material/CardActionArea';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import CardHeader from '@mui/material/CardHeader';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';

import IconButton from '@mui/material/IconButton';

import CancelIcon from '@mui/icons-material/Cancel';


import ExtensionIcon from '@mui/icons-material/Extension';
import GroupWorkIcon from '@mui/icons-material/GroupWork';
import SettingsInputComponentIcon from '@mui/icons-material/SettingsInputComponent';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';




import Divider from '@mui/material/Divider';

import Grid from '@mui/material/Grid';

import { history } from '../../store/configureStore';

import {formatTimestamp} from '../../utils/time'

import { amber } from '@mui/material/colors';





var VtnConfigurationCard = (props) => {
  return (
    <Card className={ props.classes.card } style={ { margin: 5 } }>
      <CardHeader className={ props.classes.media }
                  subheader={ props.cardType }
                  action={ <IconButton size="small"
                                       onClick={ props.close }
                                       style={ { padding: 0, margin: '5px 10px' } }>
                             <CancelIcon />
                           </IconButton> } />
      <Divider variant="middle" />
      <CardActionArea onClick={ (e) => {
                                  if ( props.edit ) props.edit( e )
                                } }>
        <CardContent style={ { minHeight: 80, maxHeight: 100 } }>
          
          <Grid container spacing={ 1 }>
            <Grid container
                  >
              <Grid size={3}>
                <Avatar style={ { backgroundColor: props.color, margin: '0px 10px', height: 50, width: 50 } }>
                  { props.icon }
                </Avatar>
              </Grid>
              <Grid size={9}>
                <Typography gutterBottom
                            variant="h6"
                            component="h2"
                            align="right"
                            style={{marginRight:10}}>
                  { props.name }
                </Typography>
                <Typography component="p"
                            variant="caption"
                            align="right"
                            style={{marginRight:10}}>
                  { props.description }
                </Typography>
              </Grid>
            </Grid>
          </Grid>
        </CardContent>
      </CardActionArea>
      <Divider variant="middle" />
      <CardActions style={ { minHeight: 40, maxHeight: 40 } }>
        { props.actions }
      </CardActions>
    </Card>
  );

}


export function VtnConfigurationMarketContextCard( props ) {
  var close = null;
  if ( props.handleDeleteMarketContext ) {
    close = props.handleDeleteMarketContext
  } else if ( props.handleRemoveVenMarketContext ) {
    close = props.handleRemoveVenMarketContext
  }
  var handleEventClick = () => {
    history.push( '/event', { filters: [{type:"MARKET_CONTEXT", value: props.context.name}] });
  }
  var handleVenClick = () => {
    history.push( '/ven', { filters: [{type:"MARKET_CONTEXT", value: props.context.name}] });
  }
  return (
  <VtnConfigurationCard classes={ props.classes }
                        color={ props.context.color }
                        name={ props.context.name }
                        description={ props.context.description }
                        close={ close }
                        edit={ props.handleEditMarketContext }
                        cardType={ "Market Context" }
                        icon={ <ExtensionIcon style={ { height: 30, width: 30 } } /> }
                        actions={ [ <Button onClick={handleEventClick} key="action_marketcontext_events" size="small" color="primary"> Events </Button>
                        , <Button onClick={handleVenClick} key="action_marketcontext_vens" size="small" color="primary"> Vens </Button> ] } />
  );
}

export function VtnConfigurationGroupCard( props ) {
  var close = null;
  if ( props.handleDeleteGroup ) {
    close = props.handleDeleteGroup
  } else if ( props.handleRemoveVenGroup ) {
    close = props.handleRemoveVenGroup
  }
  var handleVenClick = () => {
    history.push( '/ven', { filters: [{type:"GROUP", value: props.group.name}] });
  }
  return (
  <VtnConfigurationCard classes={ props.classes }
                        color="#bbb"
                        name={ props.group.name }
                        description={ props.group.description }
                        close={ close }
                        edit={ props.handleEditGroup }
                        cardType={ "Group" }
                        icon={ <GroupWorkIcon style={ { height: 30, width: 30 } } /> }
                        actions={ [ <Button onClick={handleVenClick}  key="action_group_vens" size="small" color="primary"> Vens </Button> ] } />
  );
}

export function VtnConfigurationVenCard( props ) {
  var color = (props.ven.registrationId != null) ? "green" : "#bbb";
  var handleEventClick = () => {
    history.push( '/event', { filters: [{type:"VEN", value: props.ven.username}] });
  }

  return (
  <VtnConfigurationCard classes={ props.classes }
                        color={color}
                        name={ props.ven.commonName }
                        description={ props.ven.username }
                        close={ props.handleDeleteVen }
                        edit={ props.handleEditVen }
                        cardType={ "VEN" }
                        icon={ <SettingsInputComponentIcon style={ { height: 30, width: 30} } /> }
                        actions={ [ <Button onClick={handleEventClick}  key="action_group_vens" size="small" color="primary"> Events </Button> ] } />
  );
}

export function VtnConfigurationEventCard( props ) {
  var color;
  switch(props.event.descriptor.state) {
    case "ACTIVE":
      color = "green";
      break;
    case "CANCELLED":
      color = "red";
      break;
    default:
      color = "#bbb";
      break;
  }

  if(!props.event.published) {
    color = amber[700];
  }

  var handleVenClick = () => {
    history.push( '/ven', { filters: [{type:"EVENT", value: props.event.id}] });
  }

  var start = formatTimestamp(props.event.activePeriod.start);

  return (
  <VtnConfigurationCard classes={ props.classes }
                        color={ color }
                        name={ props.event.descriptor.marketContext + ":" + props.event.id}
                        description={

                        <span>
                          { start.date + " " + start.time} | { props.event.activePeriod.duration } | { props.event.activePeriod.notificationDuration }
                           
                          
                        </span>
                        }
                        close={ props.handleDeleteEvent }
                        edit={ props.handleEditEvent }
                        cardType={ "EVENT" }
                        icon={ <CalendarTodayIcon style={ { height: 30, width: 30} } /> }
                        actions={ [ <Button onClick={handleVenClick} key="action_group_vens" size="small" color="primary"> VENS </Button> ] } />
  );
}
