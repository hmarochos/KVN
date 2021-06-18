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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Integers with specific values in Program AB
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MagicNumbers {

    static int node_activation_cnt = 4;
    static int node_size = 4;
    static int max_history = 32;
    static int max_stars = 1000;
    static int max_graph_height = 100000;
    static int max_substitutions = 10000;
    static int max_recursion = 512;
    static int max_trace_length = 2048;
    static int max_loops = 10000;
}
