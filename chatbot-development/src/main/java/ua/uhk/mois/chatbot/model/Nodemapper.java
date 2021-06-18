package ua.uhk.mois.chatbot.model;
/* Program AB Reference AIML 2.0 implementation
        Copyright (C) 2013 ALICE A.I. Foundation
        Contact: info@alicebot.org

        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Library General Public
        License as published by the Free Software Foundation; either
        version 2 of the License, or (at your option) any later version.

        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Library General Public License for more details.

        You should have received a copy of the GNU Library General Public
        License along with this library; if not, write to the
        Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
        Boston, MA  02110-1301, USA.
*/

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Nodemapper data structure.  In order to minimize memory overhead this class has no methods. Operations on Nodemapper
 * objects are performed by NodemapperOperator class
 */

public class Nodemapper {

    Category category;
    int height = MagicNumbers.max_graph_height;
    StarBindings starBindings;
    HashMap<String, Nodemapper> map;
    String key;
    Nodemapper value;
    boolean shortCut;
    ArrayList<String> sets;
}


