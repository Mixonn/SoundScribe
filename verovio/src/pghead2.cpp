/////////////////////////////////////////////////////////////////////////////
// Name:        pghead2.cpp
// Author:      Laurent Pugin
// Created:     2017
// Copyright (c) Authors and others. All rights reserved.
/////////////////////////////////////////////////////////////////////////////

#include "pghead2.h"

//----------------------------------------------------------------------------

#include <assert.h>

//----------------------------------------------------------------------------

namespace vrv {

//----------------------------------------------------------------------------
// PgHead2
//----------------------------------------------------------------------------

PgHead2::PgHead2() : RunningElement("pghead2-")
{
    Reset();
}

PgHead2::~PgHead2() {}

void PgHead2::Reset()
{
    RunningElement::Reset();
}

//----------------------------------------------------------------------------
// Functor methods
//----------------------------------------------------------------------------

} // namespace vrv