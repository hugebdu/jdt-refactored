package il.ac.idc.jdt.extra;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import il.ac.idc.jdt.Point;
import il.ac.idc.jdt.extra.constraint.datamodel.Line;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by IntelliJ IDEA.
 * User: daniels
 * Date: 6/17/12
 */
public abstract class IOUtils
{
    private static final Joiner joiner = Joiner.on('\n');
    private static final Function<Line, String> toLineString = new Function<Line, String>()
    {
        @Override
        public String apply(Line segment)
        {
            return format("%s %s %s %s",
                    segment.getP1().getX(),
                    segment.getP1().getY(),
                    segment.getP2().getX(),
                    segment.getP2().getY());
        }
    };

    public static void storeSegments(File file, Iterable<Line> segments) throws IOException
    {
        Files.write(joiner.join(ImmutableList.<String>builder()
                .add(Integer.toString(size(segments)))
                .addAll(transform(segments, toLineString))
                .build()), file, Charsets.ISO_8859_1);
    }

    public static Collection<Line> loadSegments(File file) throws IOException
    {
        return Files.readLines(file, Charsets.ISO_8859_1, segmentsLineProcessor());
    }

    private static LineProcessor<Collection<Line>> segmentsLineProcessor()
    {
        return new LineProcessor<Collection<Line>>()
        {
            final List<Line> result = newLinkedList();
            Integer lineCount = null;

            @Override
            public boolean processLine(String line) throws IOException
            {
                if (lineCount == null)
                {
                    lineCount = Integer.parseInt(line);
                    return true;
                }

                if (isBlank(line))
                    return false;

                ImmutableList<String> strings = ImmutableList.copyOf(Splitter.on(' ').split(line));

                result.add(new Line(
                        new Point(
                                Double.parseDouble(strings.get(0)),
                                Double.parseDouble(strings.get(1))),
                        new Point(
                                Double.parseDouble(strings.get(2)),
                                Double.parseDouble(strings.get(3))
                        )
                ));

                return true;
            }

            @Override
            public Collection<Line> getResult()
            {
                return result;
            }
        };
    }
}
