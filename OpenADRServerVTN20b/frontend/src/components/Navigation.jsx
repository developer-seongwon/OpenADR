import React from 'react';
import { NavLink } from 'react-router';

import { connect } from 'react-redux';

import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import SettingsInputComponentIcon from '@mui/icons-material/SettingsInputComponent';

import SettingsIcon from '@mui/icons-material/Settings';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';

import AccountBoxIcon from '@mui/icons-material/AccountBox';
import ListItemButton from "@mui/material/ListItemButton";


 class NavigationMain extends React.Component {
 	render() {
 		var user = this.props.user.user;
			const hasEventAccess = user && user.roles 
				&& (user.roles.includes("ROLE_DRPROGRAM") || user.roles.includes("ROLE_ADMIN"));

			const hasVenAccess = user && user.roles 
				&& (user.roles.includes("ROLE_DEVICE_MANAGER") || user.roles.includes("ROLE_ADMIN"))

			const hasVtnConfigurationAccess = user && user.roles && user.roles.includes("ROLE_ADMIN")

			const hasAccountAccess = user && user.roles && user.roles.includes("ROLE_ADMIN")

		  return (
            <div>
              {(hasEventAccess) ? <ListItemButton component={ NavLink } to="/event">
              <ListItemIcon>
                <CalendarTodayIcon />
              </ListItemIcon>
              <ListItemText primary="Events" />
            </ListItemButton> : null}

              {(hasVenAccess) ?  <ListItemButton component={ NavLink } to="/ven">
                <ListItemIcon>
                  <SettingsInputComponentIcon />
                </ListItemIcon>
                <ListItemText primary="VENs" />
              </ListItemButton> : null}

              {(hasVtnConfigurationAccess) ?  <ListItemButton component={ NavLink } to="/vtn_configuration">
                <ListItemIcon>
                  <SettingsIcon />
                </ListItemIcon>
                <ListItemText primary="VTN Config" />
              </ListItemButton> : null}

              {(hasAccountAccess) ?  <ListItemButton component={ NavLink } to="/account">
               <ListItemIcon>
                 <AccountBoxIcon />
               </ListItemIcon>
               <ListItemText primary="Accounts" />
             </ListItemButton> : null}

              {/*
              <ListItem button
                        component={ NavLink }
                        to="/data">
                <ListItemIcon>
                  <ShowChartIcon />
                </ListItemIcon>
                <ListItemText primary="Data" />
              </ListItem>

              */}

            </div>
          );
 	}
}


function mapStateToProps( state ) {
  return {
    user: state.user
  };
}

function mapDispatchToProps( dispatch ) {
  return {

  };
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)( NavigationMain );

