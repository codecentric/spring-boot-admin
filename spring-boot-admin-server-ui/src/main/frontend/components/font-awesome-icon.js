/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {library} from '@fortawesome/fontawesome-svg-core';
import {faGithub} from '@fortawesome/free-brands-svg-icons/faGithub';
import {faGitter} from '@fortawesome/free-brands-svg-icons/faGitter';
import {faStackOverflow} from '@fortawesome/free-brands-svg-icons/faStackOverflow';
import {faTimesCircle as farTimesCircle} from '@fortawesome/free-regular-svg-icons/faTimesCircle';
import {faAngleDoubleRight} from '@fortawesome/free-solid-svg-icons/faAngleDoubleRight';
import {faBan} from '@fortawesome/free-solid-svg-icons/faBan';
import {faBell} from '@fortawesome/free-solid-svg-icons/faBell';
import {faBellSlash} from '@fortawesome/free-solid-svg-icons/faBellSlash';
import {faBook} from '@fortawesome/free-solid-svg-icons/faBook';
import {faCheck} from '@fortawesome/free-solid-svg-icons/faCheck';
import {faCube} from '@fortawesome/free-solid-svg-icons/faCube';
import {faCubes} from '@fortawesome/free-solid-svg-icons/faCubes';
import {faDownload} from '@fortawesome/free-solid-svg-icons/faDownload';
import {faExclamation} from '@fortawesome/free-solid-svg-icons/faExclamation';
import {faExclamationTriangle} from '@fortawesome/free-solid-svg-icons/faExclamationTriangle';
import {faFilter} from '@fortawesome/free-solid-svg-icons/faFilter';
import {faHeartbeat} from '@fortawesome/free-solid-svg-icons/faHeartbeat';
import {faHistory} from '@fortawesome/free-solid-svg-icons/faHistory';
import {faHome} from '@fortawesome/free-solid-svg-icons/faHome';
import {faMapMarkerAlt} from '@fortawesome/free-solid-svg-icons/faMapMarkerAlt';
import {faMinusCircle} from '@fortawesome/free-solid-svg-icons/faMinusCircle';
import {faPencilAlt} from '@fortawesome/free-solid-svg-icons/faPencilAlt';
import {faQuestionCircle} from '@fortawesome/free-solid-svg-icons/faQuestionCircle';
import {faSearch} from '@fortawesome/free-solid-svg-icons/faSearch';
import {faSignOutAlt} from '@fortawesome/free-solid-svg-icons/faSignOutAlt';
import {faStepBackward} from '@fortawesome/free-solid-svg-icons/faStepBackward';
import {faStepForward} from '@fortawesome/free-solid-svg-icons/faStepForward';
import {faTimesCircle} from '@fortawesome/free-solid-svg-icons/faTimesCircle';
import {faTrash} from '@fortawesome/free-solid-svg-icons/faTrash';
import {faUserCircle} from '@fortawesome/free-solid-svg-icons/faUserCircle';
import {faWrench} from '@fortawesome/free-solid-svg-icons/faWrench';
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome';

library.add(
//solid
  faAngleDoubleRight,
  faBan,
  faBell,
  faBellSlash,
  faBook,
  faCheck,
  faCube,
  faCubes,
  faDownload,
  faExclamation,
  faExclamationTriangle,
  faFilter,
  faHeartbeat,
  faHistory,
  faHome,
  faMapMarkerAlt,
  faMinusCircle,
  faPencilAlt,
  faQuestionCircle,
  faSearch,
  faSignOutAlt,
  faStepBackward,
  faStepForward,
  faTimesCircle,
  faTrash,
  faUserCircle,
  faWrench,
//regular
  farTimesCircle,
//brands
  faGithub,
  faGitter,
  faStackOverflow
)
;

export default FontAwesomeIcon;
