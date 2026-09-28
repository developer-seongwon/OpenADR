import React from 'react';
import { t } from '../i18n';
import { Link } from 'react-router';

const NotFoundPage = () => {
  return (
  <div>
    <h4>{ t( 'notFound.title' ) }</h4>
    <Link to="/">
    { t( 'notFound.back' ) }
    </Link>
  </div>
  );
};

export default NotFoundPage;
