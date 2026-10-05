import React from 'react';
import { t } from '../i18n';
import { NavLink } from 'react-router';

import { connect } from 'react-redux';

import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import SettingsInputComponentIcon from '@mui/icons-material/SettingsInputComponent';

import SettingsIcon from '@mui/icons-material/Settings';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import MenuBookIcon from '@mui/icons-material/MenuBook';

import AccountBoxIcon from '@mui/icons-material/AccountBox';
import ListItemButton from "@mui/material/ListItemButton";


 class NavigationMain extends React.Component {
 	render() {
 		var user = this.props.user.user;
			// 등록 가이드는 문서라 역할과 상관없이 로그인하면 보인다
			const hasGuideAccess = Boolean(user);

			const hasEventAccess = user && user.roles 
				&& (user.roles.includes("ROLE_DRPROGRAM") || user.roles.includes("ROLE_ADMIN"));

			const hasVenAccess = user && user.roles 
				&& (user.roles.includes("ROLE_DEVICE_MANAGER") || user.roles.includes("ROLE_ADMIN"))

			const hasVtnConfigurationAccess = user && user.roles && user.roles.includes("ROLE_ADMIN")

			const hasAccountAccess = user && user.roles && user.roles.includes("ROLE_ADMIN")

		  return (
            <div>
              {(hasGuideAccess) ? <ListItemButton component={ NavLink } to="/guide">
              <ListItemIcon>
                <MenuBookIcon />
              </ListItemIcon>
              <ListItemText primary={ t( 'nav.guide' ) } />
            </ListItemButton> : null}

              {(hasEventAccess) ? <ListItemButton component={ NavLink } to="/event">
              <ListItemIcon>
                <CalendarTodayIcon />
              </ListItemIcon>
              <ListItemText primary={ t( 'nav.events' ) } />
            </ListItemButton> : null}

              {(hasVenAccess) ?  <ListItemButton component={ NavLink } to="/ven">
                <ListItemIcon>
                  <SettingsInputComponentIcon />
                </ListItemIcon>
                <ListItemText primary={ t( 'nav.vens' ) } />
              </ListItemButton> : null}

              {(hasVtnConfigurationAccess) ?  <ListItemButton component={ NavLink } to="/vtn_configuration">
                <ListItemIcon>
                  <SettingsIcon />
                </ListItemIcon>
                <ListItemText primary={ t( 'nav.vtnConfig' ) } />
              </ListItemButton> : null}

              {(hasAccountAccess) ?  <ListItemButton component={ NavLink } to="/account">
               <ListItemIcon>
                 <AccountBoxIcon />
               </ListItemIcon>
               <ListItemText primary={ t( 'nav.accounts' ) } />
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

