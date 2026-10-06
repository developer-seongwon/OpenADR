import React from 'react';
import { t } from '../i18n';
import { Link } from 'react-router';

const HomePage = () => {
  return (
  <div>
    <h1>{ t( 'home.title' ) }</h1>
    <h2>{ t( 'home.getStarted' ) }</h2>
    <ol>
      <li>
        { t( 'home.review' ) }{ ' ' }
        <Link to="/fuel-savings">
        { t( 'home.demoApp' ) }
        </Link>
      </li>
      <li>
        { t( 'home.removeDemo' ) }
      </li>
    </ol>
  </div>
  );
};

export default HomePage;
