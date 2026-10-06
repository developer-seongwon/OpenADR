import React from 'react';
import { t } from '../i18n';
import { Link } from 'react-router';
import '../styles/about-page.css';

// Since this component is simple and static, there's no parent container for it.
const AboutPage = () => {
  return (
  <div>
    <h2 className="alt-header">{ t( 'about.title' ) }</h2>
    <p>
      { t( 'about.partOf' ) } <a href="https://github.com/coryhouse/react-slingshot">{ t( 'about.starterKit' ) }</a>
    </p>
    <p>
      <Link to="/badlink">
      { t( 'about.badLink' ) }
      </Link> { t( 'about.see404' ) }
    </p>
  </div>
  );
};

export default AboutPage;
