/*
 * Copyright 2014-2018 the original author or authors.
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

import fontawesome from '@fortawesome/fontawesome';
import faGithub from '@fortawesome/fontawesome-free-brands/faGithub';
import faGitter from '@fortawesome/fontawesome-free-brands/faGitter';
import faStackOverflow from '@fortawesome/fontawesome-free-brands/faStackOverflow';
import farTimesCircle from '@fortawesome/fontawesome-free-regular/faTimesCircle';
import faBan from '@fortawesome/fontawesome-free-solid/faBan';
import faBook from '@fortawesome/fontawesome-free-solid/faBook';
import faCheck from '@fortawesome/fontawesome-free-solid/faCheck';
import faDownload from '@fortawesome/fontawesome-free-solid/faDownload';
import faExclamation from '@fortawesome/fontawesome-free-solid/faExclamation';
import faExclamationTriangle from '@fortawesome/fontawesome-free-solid/faExclamationTriangle';
import faMinusCircle from '@fortawesome/fontawesome-free-solid/faMinusCircle';
import faPencilAlt from '@fortawesome/fontawesome-free-solid/faPencilAlt';
import faQuestionCircle from '@fortawesome/fontawesome-free-solid/faQuestionCircle';
import faSignOutAlt from '@fortawesome/fontawesome-free-solid/faSignOutAlt';
import faStepBackward from '@fortawesome/fontawesome-free-solid/faStepBackward';
import faStepForward from '@fortawesome/fontawesome-free-solid/faStepForward';
import faTimesCircle from '@fortawesome/fontawesome-free-solid/faTimesCircle';
import faTrash from '@fortawesome/fontawesome-free-solid/faTrash';
import faBell from '@fortawesome/fontawesome-free-solid/faBell';
import faBellSlash from '@fortawesome/fontawesome-free-solid/faBellSlash';
import FontAwesomeIcon from '@fortawesome/vue-fontawesome';

//solid
fontawesome.library.add(faTrash, faDownload, faStepForward, faStepBackward, faCheck, faQuestionCircle, faBan, faTimesCircle, faMinusCircle, faExclamation,
  faBook, faSignOutAlt, faExclamationTriangle, faPencilAlt, faBell, faBellSlash);
//regular
fontawesome.library.add(farTimesCircle);
//brands
fontawesome.library.add(faGithub, faStackOverflow, faGitter);

export default FontAwesomeIcon;
